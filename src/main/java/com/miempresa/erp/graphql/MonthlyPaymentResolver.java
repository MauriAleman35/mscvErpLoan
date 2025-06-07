package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Loan;
import com.miempresa.erp.domain.MonthlyPayment;
import com.miempresa.erp.dto.PaymentDetailDTO;
import com.miempresa.erp.repository.LoanRepository;
import com.miempresa.erp.repository.MonthlyPaymentRepository;
import com.miempresa.erp.services.MonthlyPaymentService;
import java.time.Instant;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class MonthlyPaymentResolver {

    private final MonthlyPaymentRepository monthlyPaymentRepository;
    private final LoanRepository loanRepository;
    private final MonthlyPaymentService monthlyPaymentService;

    public MonthlyPaymentResolver(
        MonthlyPaymentRepository monthlyPaymentRepository,
        LoanRepository loanRepository,
        MonthlyPaymentService monthlyPaymentService
    ) {
        this.monthlyPaymentRepository = monthlyPaymentRepository;
        this.loanRepository = loanRepository;
        this.monthlyPaymentService = monthlyPaymentService;
    }

    @SchemaMapping(typeName = "MonthlyPayment", field = "dueDate")
    public String dueDate(MonthlyPayment payment) {
        if (payment.getDueDate() == null) {
            return null;
        }
        return payment.getDueDate().toString();
    }

    @SchemaMapping(typeName = "MonthlyPayment", field = "paymentDate")
    public String paymentDate(MonthlyPayment payment) {
        if (payment.getPaymentDate() == null) {
            return null;
        }
        return payment.getPaymentDate().toString();
    }

    // Mutations
    @MutationMapping
    public MonthlyPayment createMonthlyPayment(@Argument MonthlyPaymentInput input) {
        MonthlyPayment monthlyPayment = new MonthlyPayment();
        mapMonthlyPaymentInputToEntity(input, monthlyPayment);

        if (input.getLoanId() != null) {
            Loan loan = loanRepository.findById(input.getLoanId()).orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
            monthlyPayment.setLoan(loan);
        }

        return monthlyPaymentRepository.save(monthlyPayment);
    }

    @MutationMapping
    public MonthlyPayment updateMonthlyPayment(@Argument Long id, @Argument MonthlyPaymentInput input) {
        MonthlyPayment monthlyPayment = monthlyPaymentRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Pago mensual no encontrado"));

        mapMonthlyPaymentInputToEntity(input, monthlyPayment);

        if (input.getLoanId() != null) {
            Loan loan = loanRepository.findById(input.getLoanId()).orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));
            monthlyPayment.setLoan(loan);
        }

        return monthlyPaymentRepository.save(monthlyPayment);
    }

    @MutationMapping
    public Boolean deleteMonthlyPayment(@Argument Long id) {
        try {
            monthlyPaymentRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @MutationMapping
    public MonthlyPayment verifyBorrowerPayment(@Argument Long id, @Argument Boolean verified) {
        MonthlyPayment monthlyPayment = monthlyPaymentRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Pago mensual no encontrado"));

        monthlyPayment.setBorrowVerified(verified);

        // Si está siendo verificado, actualizar la fecha de pago
        if (verified) {
            monthlyPayment.setPaymentDate(Instant.now());
        }

        return monthlyPaymentRepository.save(monthlyPayment);
    }

    @MutationMapping
    public MonthlyPayment verifyPartnerPayment(@Argument Long id, @Argument Boolean verified) {
        MonthlyPayment monthlyPayment = monthlyPaymentRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Pago mensual no encontrado"));

        monthlyPayment.setPartnerVerified(verified);
        return monthlyPaymentRepository.save(monthlyPayment);
    }

    @QueryMapping
    public List<PaymentDetailDTO> paidMonthlyPaymentsByUser(@Argument Long userId) {
        return monthlyPaymentService.getSimplifiedPaymentsByUser(userId, "pagado");
    }

    @QueryMapping
    public List<MonthlyPayment> paymentsToVerify(@Argument Long partnerId) {
        return monthlyPaymentRepository.findByLoanOfferPartnerIdAndPaymentStatusAndPartnerVerifiedOrderByPaymentDateAsc(
            partnerId,
            "pendiente",
            false
        );
    }

    @MutationMapping
    @Transactional
    public MonthlyPayment verifyPayment(@Argument Long id, @Argument Boolean verified) {
        MonthlyPayment payment = monthlyPaymentRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Pago mensual no encontrado"));

        // Verificar que esté pendiente de verificación
        if (!"pendiente".equals(payment.getPaymentStatus())) {
            throw new RuntimeException("Este pago no está pendiente de verificación");
        }

        payment.setPartnerVerified(verified);

        if (verified) {
            payment.setPaymentStatus("pagado");

            // Actualizar el estado del préstamo si es necesario
            Loan loan = payment.getLoan();
            // Verificar si hay pagos pendientes anteriores
            boolean hasOverduePayments = monthlyPaymentRepository.existsByLoanIdAndPaymentStatusAndDueDateBefore(
                loan.getId(),
                "pendiente",
                payment.getDueDate()
            );

            if (!hasOverduePayments) {
                loan.setCurrentStatus("al_dia");
                loanRepository.save(loan);
            }
        } else {
            payment.setPaymentStatus("rechazado");
        }

        return monthlyPaymentRepository.save(payment);
    }

    @QueryMapping
    public List<MonthlyPayment> monthlyPaymentsByLoanAndStatus(@Argument Long loanId, @Argument String status) {
        return monthlyPaymentRepository.findByLoanIdAndPaymentStatusOrderByDueDateAsc(loanId, status);
    }

    private void updateLoanStatus(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        // Contar pagos pendientes
        long pendingPayments = monthlyPaymentRepository.countByLoanIdAndPaymentStatusNot(loanId, "pagado_verificado");

        // Si no hay pagos pendientes, el préstamo está completado
        if (pendingPayments == 0) {
            loan.setCurrentStatus("completado");
        } else {
            // Verificar si hay pagos atrasados
            long latePayments = monthlyPaymentRepository.countByLoanIdAndDueDateBeforeAndPaymentStatusNot(
                loanId,
                Instant.now(),
                "pagado_verificado"
            );

            if (latePayments > 0) {
                loan.setCurrentStatus("atrasado");
                loan.setLatePaymentCount((int) latePayments);
            } else {
                loan.setCurrentStatus("al_dia");
            }
        }

        loan.setLastStatusUpdate(Instant.now());
        loanRepository.save(loan);
    }

    // Helper method
    private void mapMonthlyPaymentInputToEntity(MonthlyPaymentInput input, MonthlyPayment monthlyPayment) {
        if (input.getDueDate() != null) monthlyPayment.setDueDate(input.getDueDate());
        if (input.getPaymentDate() != null) monthlyPayment.setPaymentDate(input.getPaymentDate());
        if (input.getBorrowVerified() != null) monthlyPayment.setBorrowVerified(input.getBorrowVerified());
        if (input.getPartnerVerified() != null) monthlyPayment.setPartnerVerified(input.getPartnerVerified());
        if (input.getComprobantFile() != null) monthlyPayment.setComprobantFile(input.getComprobantFile());
        if (input.getDaysLate() != null) monthlyPayment.setDaysLate(input.getDaysLate());
        if (input.getPenaltyAmount() != null) monthlyPayment.setPenaltyAmount(input.getPenaltyAmount());
        if (input.getPaymentStatus() != null) monthlyPayment.setPaymentStatus(input.getPaymentStatus());
    }
}

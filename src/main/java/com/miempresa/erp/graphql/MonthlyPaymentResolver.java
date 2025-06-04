package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Loan;
import com.miempresa.erp.domain.MonthlyPayment;
import com.miempresa.erp.repository.LoanRepository;
import com.miempresa.erp.repository.MonthlyPaymentRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MonthlyPaymentResolver {

    private final MonthlyPaymentRepository monthlyPaymentRepository;
    private final LoanRepository loanRepository;

    public MonthlyPaymentResolver(MonthlyPaymentRepository monthlyPaymentRepository, LoanRepository loanRepository) {
        this.monthlyPaymentRepository = monthlyPaymentRepository;
        this.loanRepository = loanRepository;
    }

    // Queries
    @QueryMapping
    public MonthlyPayment monthlyPayment(@Argument Long id) {
        return monthlyPaymentRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<MonthlyPayment> monthlyPaymentsByLoan(@Argument Long loanId) {
        return monthlyPaymentRepository.findByLoanId(loanId);
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

package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Loan;
import com.miempresa.erp.domain.MonthlyPayment;
import com.miempresa.erp.domain.Offer;
import com.miempresa.erp.repository.LoanRepository;
import com.miempresa.erp.repository.MonthlyPaymentRepository;
import com.miempresa.erp.repository.OfferRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class LoanResolver {

    private final LoanRepository loanRepository;
    private final OfferRepository offerRepository;
    private final MonthlyPaymentRepository monthlyPaymentRepository;

    public LoanResolver(LoanRepository loanRepository, OfferRepository offerRepository, MonthlyPaymentRepository monthlyPaymentRepository) {
        this.loanRepository = loanRepository;
        this.offerRepository = offerRepository;
        this.monthlyPaymentRepository = monthlyPaymentRepository;
    }

    // Queries
    @QueryMapping
    public Loan loan(@Argument Long id) {
        return loanRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Loan> loans(@Argument(name = "filter") LoanFilter filter, @Argument Integer page, @Argument Integer size) {
        if (filter == null) {
            return loanRepository.findAll();
        }

        // Implementar filtros básicos
        //if (filter.getCurrentStatus() != null) {
        // return loanRepository.findByCurrentStatus(filter.getCurrentStatus());
        // }

        // Si quieres implementar filtros por rango de montos
        if (filter.getMinAmount() != null && filter.getMaxAmount() != null) {
            return loanRepository.findByLoanAmountBetween(filter.getMinAmount(), filter.getMaxAmount());
        }

        return loanRepository.findAll();
    }

    @QueryMapping
    public Integer loanCount(@Argument(name = "filter") LoanFilter filter) {
        if (filter == null) {
            return loanRepository.findAll().size();
        }

        // Contar con filtros básicos (implementar según sea necesario)
        if (filter.getCurrentStatus() != null) {
            return loanRepository.countByCurrentStatus(filter.getCurrentStatus());
        }

        return loanRepository.findAll().size();
    }

    // Mutations
    @MutationMapping
    public Loan createLoan(@Argument LoanInput input) {
        Loan loan = new Loan();
        mapLoanInputToEntity(input, loan);

        if (input.getOfferId() != null) {
            offerRepository.findById(input.getOfferId()).ifPresent(loan::setOffer);
        }

        return loanRepository.save(loan);
    }

    @MutationMapping
    public Loan updateLoan(@Argument Long id, @Argument LoanInput input) {
        Loan loan = loanRepository.findById(id).orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        mapLoanInputToEntity(input, loan);

        if (input.getOfferId() != null) {
            offerRepository.findById(input.getOfferId()).ifPresent(loan::setOffer);
        }

        return loanRepository.save(loan);
    }

    @MutationMapping
    public Boolean deleteLoan(@Argument Long id) {
        try {
            loanRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @QueryMapping
    public List<MonthlyPayment> monthlyPaymentsByLoan(@Argument Long loanId) {
        return monthlyPaymentRepository.findByLoanIdOrderByDueDateAsc(loanId);
    }

    @SchemaMapping(typeName = "Loan", field = "startDate")
    public String startDate(Loan loan) {
        if (loan.getStartDate() == null) {
            return null;
        }
        // Convertir explícitamente a formato ISO con toInstant()
        return loan.getStartDate().toInstant().toString();
    }

    @SchemaMapping(typeName = "Loan", field = "endDate")
    public String endDate(Loan loan) {
        if (loan.getEndDate() == null) {
            return null;
        }
        // Convertir explícitamente a formato ISO con toInstant()
        return loan.getEndDate().toInstant().toString();
    }

    @QueryMapping
    public MonthlyPayment monthlyPayment(@Argument Long id) {
        return monthlyPaymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Pago mensual no encontrado"));
    }

    @QueryMapping
    public List<Loan> loansByPartnerId(@Argument Long partnerId) {
        return loanRepository.findLoansByPartnerId(partnerId);
    }

    // Helper method
    private void mapLoanInputToEntity(LoanInput input, Loan loan) {
        if (input.getLoanAmount() != null) loan.setLoanAmount(input.getLoanAmount());
        if (input.getStartDate() != null) loan.setStartDate(input.getStartDate());
        if (input.getEndDate() != null) loan.setEndDate(input.getEndDate());
        if (input.getHashBlockchain() != null) loan.setHashBlockchain(input.getHashBlockchain());
        if (input.getCurrentStatus() != null) loan.setCurrentStatus(input.getCurrentStatus());
        if (input.getLatePaymentCount() != null) loan.setLatePaymentCount(input.getLatePaymentCount());
        if (input.getLastStatusUpdate() != null) loan.setLastStatusUpdate(input.getLastStatusUpdate());
    }
}

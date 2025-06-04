package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Loan;
import com.miempresa.erp.domain.Offer;
import com.miempresa.erp.domain.Solicitude;
import com.miempresa.erp.repository.LoanRepository;
import com.miempresa.erp.repository.OfferRepository;
import com.miempresa.erp.repository.SolicitudeRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class OfferResolver {

    private final OfferRepository offerRepository;
    private final SolicitudeRepository solicitudeRepository;
    private final LoanRepository loanRepository;

    public OfferResolver(OfferRepository offerRepository, SolicitudeRepository solicitudeRepository, LoanRepository loanRepository) {
        this.offerRepository = offerRepository;
        this.solicitudeRepository = solicitudeRepository;
        this.loanRepository = loanRepository;
    }

    // Queries
    @QueryMapping
    public Offer offer(@Argument Long id) {
        return offerRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Offer> offers(@Argument(name = "filter") OfferFilter filter, @Argument Integer page, @Argument Integer size) {
        if (filter == null) {
            return offerRepository.findAll();
        }

        // Implementar filtros básicos
        if (filter.getStatus() != null) {
            return offerRepository.findByStatus(filter.getStatus());
        }

        if (filter.getPartnerId() != null) {
            return offerRepository.findByPartnerId(filter.getPartnerId());
        }

        if (filter.getSolicitudeId() != null) {
            return offerRepository.findBySolicitudeId(filter.getSolicitudeId());
        }

        return offerRepository.findAll();
    }

    @QueryMapping
    public Integer offerCount(@Argument(name = "filter") OfferFilter filter) {
        if (filter == null) {
            return offerRepository.findAll().size();
        }

        // Contar con filtros
        if (filter.getStatus() != null) {
            return offerRepository.countByStatus(filter.getStatus());
        }

        return offerRepository.findAll().size();
    }

    // Mutations
    @MutationMapping
    public Offer createOffer(@Argument OfferInput input) {
        Offer offer = new Offer();
        mapOfferInputToEntity(input, offer);

        if (input.getSolicitudeId() != null) {
            Solicitude solicitude = solicitudeRepository
                .findById(input.getSolicitudeId())
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            offer.setSolicitude(solicitude);
        }

        // Establecer fecha de creación
        offer.setCreatedAt(Instant.now());

        return offerRepository.save(offer);
    }

    @MutationMapping
    public Offer updateOffer(@Argument Long id, @Argument OfferInput input) {
        Offer offer = offerRepository.findById(id).orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

        mapOfferInputToEntity(input, offer);

        if (input.getSolicitudeId() != null) {
            Solicitude solicitude = solicitudeRepository
                .findById(input.getSolicitudeId())
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
            offer.setSolicitude(solicitude);
        }

        return offerRepository.save(offer);
    }

    @MutationMapping
    public Boolean deleteOffer(@Argument Long id) {
        try {
            offerRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @MutationMapping
    public Loan acceptOffer(@Argument Long id) {
        Offer offer = offerRepository.findById(id).orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

        // Cambiar estado de la oferta a aceptada
        offer.setStatus("ACCEPTED");
        offerRepository.save(offer);

        // Crear nuevo préstamo basado en la oferta
        Loan loan = new Loan();
        loan.setOffer(offer);
        loan.setLoanAmount(offer.getSolicitude().getLoanAmount());
        loan.setStartDate(Instant.now());
        // Calcular fecha de finalización basada en el término del préstamo (meses)
        // Aquí simplificamos, pero podrías implementar un cálculo más preciso
        loan.setCurrentStatus("ACTIVE");
        loan.setLatePaymentCount(0);
        loan.setLastStatusUpdate(Instant.now());

        return loanRepository.save(loan);
    }

    // Helper method
    private void mapOfferInputToEntity(OfferInput input, Offer offer) {
        if (input.getPartnerId() != null) offer.setPartnerId(input.getPartnerId());
        if (input.getInterest() != null) offer.setInterest(input.getInterest());
        if (input.getLoanTerm() != null) offer.setLoanTerm(input.getLoanTerm());
        if (input.getMonthlyPayment() != null) offer.setMonthlyPayment(input.getMonthlyPayment());
        if (input.getTotalRepaymentAmount() != null) offer.setTotalRepaymentAmount(input.getTotalRepaymentAmount());
        if (input.getStatus() != null) offer.setStatus(input.getStatus());
    }
}

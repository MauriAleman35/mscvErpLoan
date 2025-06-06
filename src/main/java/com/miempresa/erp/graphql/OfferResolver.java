package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.*;
import com.miempresa.erp.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class OfferResolver {

    private final OfferRepository offerRepository;
    private final SolicitudeRepository solicitudeRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final MonthlyPaymentRepository monthlyPaymentRepository;

    public OfferResolver(
        OfferRepository offerRepository,
        SolicitudeRepository solicitudeRepository,
        LoanRepository loanRepository,
        UserRepository userRepository,
        MonthlyPaymentRepository monthlyPaymentRepository
    ) {
        this.offerRepository = offerRepository;
        this.solicitudeRepository = solicitudeRepository;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.monthlyPaymentRepository = monthlyPaymentRepository;
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
    @Transactional
    public Offer createOffer(@Argument OfferInput input) {
        Solicitude solicitude = solicitudeRepository
            .findById(input.getSolicitudeId())
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Verificar estado de la solicitud
        if (!"pendiente".equals(solicitude.getStatus())) {
            throw new RuntimeException("Esta solicitud no está disponible para ofertas");
        }

        // Cálculos financieros
        BigDecimal loanAmount = solicitude.getLoanAmount();
        BigDecimal interestRate = input.getInterest().divide(BigDecimal.valueOf(100));
        int term = input.getLoanTerm();

        // Cálculo de interés total: monto * tasa mensual * número de meses
        BigDecimal totalInterest = loanAmount.multiply(interestRate).multiply(BigDecimal.valueOf(term));

        // Monto total a pagar: monto original + interés total
        BigDecimal totalRepayment = loanAmount.add(totalInterest);

        // Pago mensual: monto total / número de meses
        BigDecimal monthlyPayment = totalRepayment.divide(BigDecimal.valueOf(term), 2, RoundingMode.HALF_UP);

        // Crear la oferta
        Offer offer = new Offer();
        offer.setPartnerId(input.getPartnerId());
        offer.setInterest(input.getInterest());
        offer.setLoanTerm(input.getLoanTerm());
        offer.setSolicitude(solicitude);
        offer.setMonthlyPayment(monthlyPayment);
        offer.setTotalRepaymentAmount(totalRepayment);
        offer.setStatus("pendiente");

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
    @Transactional
    public Loan acceptOffer(@Argument Long id) { // Añadida la anotación @Argument
        // 1. Encontrar la oferta por ID
        Offer offer = offerRepository.findById(id).orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

        // 2. Verificar que la oferta esté pendiente
        if (!"pendiente".equals(offer.getStatus())) {
            throw new RuntimeException("Esta oferta ya no está disponible");
        }

        Solicitude solicitude = offer.getSolicitude();

        // 3. Verificar que la solicitud esté activa
        if (!"pendiente".equals(solicitude.getStatus())) {
            throw new RuntimeException("Esta solicitud ya no está disponible");
        }

        // 4. Cambiar estado de la oferta a ACEPTADA
        offer.setStatus("aceptada");
        offerRepository.save(offer);

        // 5. Rechazar otras ofertas para esta solicitud
        List<Offer> otherOffers = offerRepository.findBySolicitudeAndStatusNot(solicitude, "aceptada");

        for (Offer otherOffer : otherOffers) {
            if (!otherOffer.getId().equals(offer.getId()) && "pendiente".equals(otherOffer.getStatus())) {
                otherOffer.setStatus("rechazada"); // Cambio a "rechazada" (corregido)
                offerRepository.save(otherOffer);
            }
        }

        // 6. Actualizar estado de la solicitud
        solicitude.setStatus("completada");
        solicitudeRepository.save(solicitude);

        // 7. Crear el préstamo (loan)
        Loan loan = new Loan();
        loan.setOffer(offer);
        loan.setLoanAmount(solicitude.getLoanAmount());

        // Establecer fecha de inicio un mes después de la aceptación
        LocalDateTime acceptanceDate = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime startLocalDateTime = acceptanceDate.plusMonths(1);
        Instant startInstant = startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant();
        loan.setStartDate(startInstant);

        loan.setHashBlockchain("pendiente");

        // Calcular fecha final (startDate + loanTerm meses)
        LocalDateTime endLocalDateTime = startLocalDateTime.plusMonths(offer.getLoanTerm());
        loan.setEndDate(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());

        loan.setCurrentStatus("al_dia");
        loan.setLatePaymentCount(0);
        loan.setLastStatusUpdate(Instant.now());

        Loan savedLoan = loanRepository.save(loan);

        // 8. Generar cuotas mensuales (CORREGIDO)
        List<MonthlyPayment> payments = new ArrayList<>();

        // Usar LocalDateTime para manejar fechas con mayor precisión
        LocalDateTime startDate = LocalDateTime.ofInstant(loan.getStartDate(), ZoneId.systemDefault());

        // Dividir el monto total entre el número de cuotas
        BigDecimal paymentAmount = offer.getTotalRepaymentAmount().divide(BigDecimal.valueOf(offer.getLoanTerm()), 2, RoundingMode.HALF_UP);

        LocalDateTime dueDateTime = startDate; // Inicializar con fecha de inicio

        for (int i = 1; i <= offer.getLoanTerm(); i++) {
            // Avanzar un mes para esta cuota
            dueDateTime = dueDateTime.plusMonths(1);

            MonthlyPayment payment = new MonthlyPayment();
            payment.setLoan(savedLoan);
            payment.setDueDate(dueDateTime.atZone(ZoneId.systemDefault()).toInstant());

            // Campos obligatorios
            payment.setPaymentDate(null); // Aún no se ha pagado
            payment.setComprobantFile("pendiente");
            payment.setPenaltyAmount(BigDecimal.ZERO);
            payment.setPaymentStatus("pendiente");
            payment.setBorrowVerified(false);
            payment.setPartnerVerified(false);
            payment.setDaysLate(0);

            // Guardar y agregar al listado
            payments.add(monthlyPaymentRepository.save(payment));

            // Mensaje de log para verificar
            System.out.println("Cuota " + i + " creada con vencimiento: " + dueDateTime);
        }

        // Registrar cuántas cuotas se crearon
        System.out.println("Total de cuotas creadas: " + payments.size() + " para un préstamo de " + offer.getLoanTerm() + " meses");

        return savedLoan;
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

    @QueryMapping
    public List<Offer> pendingOffersBySolicitude(@Argument Long solicitudeId) {
        return offerRepository.findBySolicitudeIdAndStatus(solicitudeId, "pendiente");
    }
}

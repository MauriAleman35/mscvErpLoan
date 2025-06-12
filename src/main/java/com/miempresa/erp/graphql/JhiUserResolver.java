package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Loan;
import com.miempresa.erp.domain.Offer;
import com.miempresa.erp.domain.Solicitude;
import com.miempresa.erp.domain.User;
import com.miempresa.erp.dto.BorrowerStats;
import com.miempresa.erp.event.EventPublisher;
import com.miempresa.erp.graphql.JhiUserInput;
import com.miempresa.erp.graphql.UserFilter;
import com.miempresa.erp.repository.LoanRepository;
import com.miempresa.erp.repository.OfferRepository;
import com.miempresa.erp.repository.SolicitudeRepository;
import com.miempresa.erp.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

@Controller
public class JhiUserResolver {

    private final UserRepository jhiUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolicitudeRepository solicitudeRepository;
    private final OfferRepository offerRepository;
    private final LoanRepository loanRepository;
    private final EventPublisher eventPublisher;

    public JhiUserResolver(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        SolicitudeRepository solicitudeRepository,
        OfferRepository offerRepository,
        LoanRepository loanRepository,
        EventPublisher eventPublisher
    ) {
        this.jhiUserRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.solicitudeRepository = solicitudeRepository;
        this.offerRepository = offerRepository;
        this.loanRepository = loanRepository;
        this.eventPublisher = eventPublisher;
    }

    // Queries
    @QueryMapping
    public User user(@Argument Long id) {
        return jhiUserRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public User userByEmail(@Argument String email) {
        return jhiUserRepository.findOneByEmail(email).orElse(null);
    }

    @QueryMapping
    public List<User> users(@Argument(name = "filter") UserFilter filter, @Argument Integer page, @Argument Integer size) {
        if (filter == null) {
            return jhiUserRepository.findAll();
        }

        // Implementar filtros básicos
        if (filter.getEmail() != null) {
            return jhiUserRepository.findByEmailContaining(filter.getEmail());
        }

        if (filter.getUserType() != null) {
            return jhiUserRepository.findByUserType(filter.getUserType());
        }

        if (filter.getStatus() != null) {
            return jhiUserRepository.findByStatus(filter.getStatus());
        }

        return jhiUserRepository.findAll();
    }

    // Mutations
    @MutationMapping
    public User createUser(@Argument JhiUserInput input) {
        User user = new User();

        // Establecemos primero los campos básicos
        mapUserInputToEntityWithoutPassword(input, user);

        // Encriptamos la contraseña correctamente
        if (input.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        User saved = jhiUserRepository.save(user);
        eventPublisher.publishChange("user", "insert", saved);
        return saved;
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument JhiUserInput input) {
        User user = jhiUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizamos los campos excepto la contraseña
        mapUserInputToEntityWithoutPassword(input, user);

        // Si se proporciona una nueva contraseña, la encriptamos
        if (input.getPassword() != null && !input.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        User updated = jhiUserRepository.save(user);
        eventPublisher.publishChange("user", "update", updated);
        return updated;
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        try {
            User user = jhiUserRepository.findById(id).orElse(null);
            if (user == null) return false;

            jhiUserRepository.deleteById(id);
            eventPublisher.publishChange("user", "delete", user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Agregar método para prestamistas si es necesario
    public List<Loan> activeLoansByPartner(Integer partnerId) {
        return loanRepository.findActiveLoansByPartnerId(partnerId);
    }

    @MutationMapping
    public User verifyUserIdentity(@Argument Long id, @Argument Boolean verified) {
        User user = jhiUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setIdentityVerified(verified);
        User updated = jhiUserRepository.save(user);
        eventPublisher.publishChange("user", "update", updated);
        return updated;
    }

    @MutationMapping
    public User verifyUserAddress(@Argument Long id, @Argument Boolean verified) {
        User user = jhiUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setAddressVerified(verified);
        User updated = jhiUserRepository.save(user);
        eventPublisher.publishChange("user", "update", updated);
        return updated;
    }

    // Para prestatarios: ver ofertas recibidas para una solicitud
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @QueryMapping
    public List<Offer> offersBySolicitude(@Argument Long solicitudeId, @Argument String status) {
        if (status != null && !status.isEmpty()) {
            return offerRepository.findBySolicitudeIdAndStatus(solicitudeId, status);
        } else {
            return offerRepository.findBySolicitudeId(solicitudeId);
        }
    }

    // Para ambos: ver préstamos activos
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @QueryMapping
    public List<Loan> activeLoansByUser(@Argument Long userId) {
        return loanRepository.findActiveLoansByBorrowerId(userId);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @QueryMapping
    public List<Loan> activeLoansByPartner(@Argument Long userId) {
        return loanRepository.findActiveLoansByPartnerId(userId);
    }

    // Helper method - sin procesar la contraseña
    private void mapUserInputToEntityWithoutPassword(JhiUserInput input, User user) {
        if (input.getName() != null) user.setName(input.getName());
        if (input.getLastName() != null) user.setLastName(input.getLastName());
        if (input.getEmail() != null) user.setEmail(input.getEmail());
        if (input.getPhone() != null) user.setPhone(input.getPhone());
        if (input.getCi() != null) user.setCi(input.getCi());
        // No procesamos la contraseña aquí
        if (input.getScore() != null) user.setScore(input.getScore());
        if (input.getStatus() != null) user.setStatus(input.getStatus());
        if (input.getUserType() != null) user.setUserType(input.getUserType());
        if (input.getAdressVerified() != null) user.setAddressVerified(input.getAdressVerified());
        if (input.getIdentityVerified() != null) user.setIdentityVerified(input.getIdentityVerified());
    }
}

package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Solicitude;
import com.miempresa.erp.domain.User;
import com.miempresa.erp.repository.SolicitudeRepository;
import com.miempresa.erp.repository.UserRepository;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SolicitudeResolver {

    private final SolicitudeRepository solicitudeRepository;
    private final UserRepository jhiUserRepository;

    public SolicitudeResolver(SolicitudeRepository solicitudeRepository, UserRepository jhiUserRepository) {
        this.solicitudeRepository = solicitudeRepository;
        this.jhiUserRepository = jhiUserRepository;
    }

    // Queries
    @QueryMapping
    public Solicitude solicitude(@Argument Long id) {
        return solicitudeRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Solicitude> solicitudes(
        @Argument(name = "filter") SolicitudeFilter filter,
        @Argument Integer page,
        @Argument Integer size
    ) {
        if (filter == null) {
            return solicitudeRepository.findAll();
        }

        // Implementar filtros básicos
        if (filter.getStatus() != null) {
            return solicitudeRepository.findByStatus(filter.getStatus());
        }

        if (filter.getBorrowerId() != null) {
            return solicitudeRepository.findByBorrowerId(filter.getBorrowerId());
        }

        return solicitudeRepository.findAll();
    }

    @QueryMapping
    public Integer solicitudeCount(@Argument(name = "filter") SolicitudeFilter filter) {
        if (filter == null) {
            return solicitudeRepository.findAll().size();
        }

        // Contar con filtros (implementar según sea necesario)
        if (filter.getStatus() != null) {
            return solicitudeRepository.countByStatus(filter.getStatus());
        }

        return solicitudeRepository.findAll().size();
    }

    // Mutations
    @MutationMapping
    public Solicitude createSolicitude(@Argument SolicitudeInput input) {
        Solicitude solicitude = new Solicitude();
        mapSolicitudeInputToEntity(input, solicitude);

        if (input.getBorrowerId() != null) {
            User borrower = jhiUserRepository
                .findById(input.getBorrowerId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            solicitude.setBorrower(borrower);
        }

        return solicitudeRepository.save(solicitude);
    }

    @MutationMapping
    public Solicitude updateSolicitude(@Argument Long id, @Argument SolicitudeInput input) {
        Solicitude solicitude = solicitudeRepository.findById(id).orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        mapSolicitudeInputToEntity(input, solicitude);

        if (input.getBorrowerId() != null) {
            User borrower = jhiUserRepository
                .findById(input.getBorrowerId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            solicitude.setBorrower(borrower);
        }

        return solicitudeRepository.save(solicitude);
    }

    @MutationMapping
    public Boolean deleteSolicitude(@Argument Long id) {
        try {
            solicitudeRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method
    private void mapSolicitudeInputToEntity(SolicitudeInput input, Solicitude solicitude) {
        if (input.getLoanAmount() != null) solicitude.setLoanAmount(input.getLoanAmount());
        if (input.getStatus() != null) solicitude.setStatus(input.getStatus());
    }
}

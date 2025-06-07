package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Solicitude;
import com.miempresa.erp.domain.User;
import com.miempresa.erp.dto.SolicitudeDTO;
import com.miempresa.erp.repository.SolicitudeRepository;
import com.miempresa.erp.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        // if (filter.getStatus() != null) {
        //  return solicitudeRepository.findByStatus(filter.getStatus());
        // }

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
    public Map<String, Object> createSolicitude(@Argument SolicitudeInput input) {
        User borrower = jhiUserRepository.findById(input.getBorrowerId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Solicitude solicitude = new Solicitude();
        solicitude.setLoanAmount(input.getLoanAmount());
        solicitude.setStatus(input.getStatus() != null ? input.getStatus() : "pendiente");
        solicitude.setBorrower(borrower);

        Solicitude savedSolicitude = solicitudeRepository.save(solicitude);

        // Crear un mapa para evitar problemas de serialización
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedSolicitude.getId().toString());
        result.put("loanAmount", savedSolicitude.getLoanAmount());
        result.put("status", savedSolicitude.getStatus());
        result.put("createdAt", savedSolicitude.getCreatedAt().toString()); // Convertir a String

        return result;
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

    /**
     *
     *
     *
     */

    @QueryMapping
    public List<SolicitudeDTO> availableSolicitudes(@Argument Integer page, @Argument Integer size, @Argument Integer daysBack) {
        // Definir fecha de inicio (por defecto 30 días si no se especifica)
        int days = daysBack != null ? daysBack : 30;
        LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);

        List<Solicitude> solicitudes;
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            solicitudes = solicitudeRepository.findRecentByStatus("pendiente", startDate, pageable);
        } else {
            solicitudes = solicitudeRepository.findRecentByStatus("pendiente", startDate);
        }

        // Convertir entidades a DTOs para manejar correctamente las fechas
        return solicitudes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private SolicitudeDTO convertToDTO(Solicitude solicitude) {
        SolicitudeDTO dto = new SolicitudeDTO();
        dto.setId(solicitude.getId());
        dto.setLoanAmount(solicitude.getLoanAmount());
        dto.setStatus(solicitude.getStatus());

        // Convertir Timestamp a OffsetDateTime
        if (solicitude.getCreatedAt() != null) {
            dto.setCreatedAt(solicitude.getCreatedAt().toLocalDateTime().atOffset(ZoneOffset.UTC));
        }

        // Mapear otras propiedades según sea necesario
        dto.setBorrower(solicitude.getBorrower());

        return dto;
    }

    // Helper method
    private void mapSolicitudeInputToEntity(SolicitudeInput input, Solicitude solicitude) {
        if (input.getLoanAmount() != null) solicitude.setLoanAmount(input.getLoanAmount());
        if (input.getStatus() != null) solicitude.setStatus(input.getStatus());
    }
}

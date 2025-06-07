package com.miempresa.erp.services;

import com.miempresa.erp.dto.PaymentDetailDTO;
import com.miempresa.erp.repository.MonthlyPaymentRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MonthlyPaymentService {

    private final MonthlyPaymentRepository monthlyPaymentRepository;

    public MonthlyPaymentService(MonthlyPaymentRepository monthlyPaymentRepository) {
        this.monthlyPaymentRepository = monthlyPaymentRepository;
    }

    public List<PaymentDetailDTO> getSimplifiedPaymentsByUser(Long userId, String status) {
        List<Object[]> results = monthlyPaymentRepository.findPaidPaymentsByUserId(userId, status);
        return results.stream().map(this::mapToSimpleDTO).collect(Collectors.toList());
    }

    private PaymentDetailDTO mapToSimpleDTO(Object[] result) {
        PaymentDetailDTO dto = new PaymentDetailDTO();

        // Mapeo simplificado y más seguro con control de índices y conversiones explícitas
        dto.setId(result[0] != null ? ((Number) result[0]).longValue() : null);
        dto.setDueDate(result[1] != null ? ((java.sql.Timestamp) result[1]).toInstant() : null);
        dto.setPaymentDate(result[2] != null ? ((java.sql.Timestamp) result[2]).toInstant() : null);
        dto.setPaymentStatus(result[3] != null ? result[3].toString() : null);
        dto.setComprobantFile(result[4] != null ? result[4].toString() : null);
        dto.setPenaltyAmount(result[5] != null ? new BigDecimal(result[5].toString()) : null);
        dto.setExpectedPayment(result[6] != null ? new BigDecimal(result[6].toString()) : null);
        dto.setCuotaNumber(result[7] != null ? ((Number) result[7]).intValue() : null);
        dto.setTotalCuotas(result[8] != null ? ((Number) result[8]).intValue() : null);

        return dto;
    }
}

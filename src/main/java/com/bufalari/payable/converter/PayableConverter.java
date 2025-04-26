// Path: src/main/java/com/bufalari/payable/converter/PayableConverter.java
package com.bufalari.payable.converter;

import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.entity.PayableEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList; // Import for list initialization

/**
 * Converts between PayableEntity and PayableDTO.
 * Converte entre PayableEntity e PayableDTO.
 */
@Component
public class PayableConverter {

    /**
     * Converts PayableEntity to PayableDTO.
     * Converte PayableEntity para PayableDTO.
     */
    public PayableDTO entityToDTO(PayableEntity entity) {
        if (entity == null) {
            return null;
        }
        return PayableDTO.builder()
                .id(entity.getId())
                .supplierId(entity.getSupplierId())
                .projectId(entity.getProjectId())
                .costCenterId(entity.getCostCenterId())
                .description(entity.getDescription())
                .invoiceReference(entity.getInvoiceReference())
                .issueDate(entity.getIssueDate())
                .dueDate(entity.getDueDate())
                .paymentDate(entity.getPaymentDate())
                .amountDue(entity.getAmountDue())
                .amountPaid(entity.getAmountPaid())
                .status(entity.getStatus())
                .documentReferences(entity.getDocumentReferences() != null ? new ArrayList<>(entity.getDocumentReferences()) : new ArrayList<>())
                .build();
    }

    /**
     * Converts PayableDTO to PayableEntity.
     * Converte PayableDTO para PayableEntity.
     */
    public PayableEntity dtoToEntity(PayableDTO dto) {
        if (dto == null) {
            return null;
        }
        return PayableEntity.builder()
                .id(dto.getId()) // Keep ID for updates
                .supplierId(dto.getSupplierId())
                .projectId(dto.getProjectId())
                .costCenterId(dto.getCostCenterId())
                .description(dto.getDescription())
                .invoiceReference(dto.getInvoiceReference())
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .paymentDate(dto.getPaymentDate())
                .amountDue(dto.getAmountDue())
                .amountPaid(dto.getAmountPaid()) // Will default to 0 in @PrePersist if null
                .status(dto.getStatus()) // Will default to PENDING in @PrePersist if null
                .documentReferences(dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>())
                .build();
    }
}
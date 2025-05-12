package com.bufalari.payable.converter;

import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.dto.PaymentTransactionDTO; // Importar DTO da Transação
import com.bufalari.payable.entity.PayableEntity;
import lombok.RequiredArgsConstructor; // Usar Lombok para injeção
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections; // Importar Collections
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts between PayableEntity (with UUID ID) and PayableDTO (with UUID ID).
 * Handles mapping, including calculated fields like totalAmountPaid and balanceDue.
 * Converte entre PayableEntity (com ID UUID) e PayableDTO (com ID UUID).
 * Trata o mapeamento, incluindo campos calculados como totalAmountPaid e balanceDue.
 */
@Component
@RequiredArgsConstructor // Lombok injeta as dependências final
public class PayableConverter {

    // Inject the converter needed for nested list mapping
    // Injeta o conversor necessário para mapear a lista aninhada
    private final PaymentTransactionConverter paymentTransactionConverter;

    /**
     * Converts PayableEntity to PayableDTO.
     * Includes calculated totalAmountPaid, balanceDue, and optionally the list of transactions.
     * Converte PayableEntity para PayableDTO.
     * Inclui totalAmountPaid calculado, balanceDue, e opcionalmente a lista de transações.
     *
     * @param entity The PayableEntity (with UUID ID) to convert. / A entidade PayableEntity (com ID UUID) para converter.
     * @return The corresponding PayableDTO (with UUID ID). / O PayableDTO (com ID UUID) correspondente.
     */
    public PayableDTO entityToDTO(PayableEntity entity) {
        if (entity == null) {
            return null;
        }

        // Use the helper methods from the entity for calculated fields
        // Usa os métodos auxiliares da entidade para campos calculados
        BigDecimal totalPaid = entity.getTotalAmountPaid();
        BigDecimal balanceDue = entity.getBalanceDue();

        // Convert payment transactions if the list is available/loaded
        // Converte as transações de pagamento se a lista estiver disponível/carregada
        List<PaymentTransactionDTO> transactionDTOs;
        if (entity.getPaymentTransactions() != null) {
            transactionDTOs = entity.getPaymentTransactions().stream()
                    .map(paymentTransactionConverter::entityToDTO) // Use the injected converter
                    .collect(Collectors.toList());
        } else {
            transactionDTOs = Collections.emptyList(); // Use lista vazia se nulo
        }

        return PayableDTO.builder()
                .id(entity.getId()) // <<<--- UUID
                .supplierId(entity.getSupplierId())
                .projectId(entity.getProjectId())
                .costCenterId(entity.getCostCenterId())
                .description(entity.getDescription())
                .invoiceReference(entity.getInvoiceReference())
                .issueDate(entity.getIssueDate())
                .dueDate(entity.getDueDate())
                // paymentDate removed / paymentDate removido
                .amountDue(entity.getAmountDue())
                // amountPaid removed / amountPaid removido
                .totalAmountPaid(totalPaid) // Map calculated value / Mapeia valor calculado
                .balanceDue(balanceDue)     // Map calculated value / Mapeia valor calculado
                .status(entity.getStatus())
                .documentReferences(entity.getDocumentReferences() != null ? new ArrayList<>(entity.getDocumentReferences()) : new ArrayList<>())
                .paymentTransactions(transactionDTOs) // Include the list of transactions / Inclui a lista de transações
                .build();
    }

    /**
     * Converts PayableDTO to PayableEntity.
     * Note: Calculated fields (totalAmountPaid, balanceDue) and the list of paymentTransactions
     * from the DTO are ignored during this conversion, as they are managed by the service/entity logic.
     * Converte PayableDTO para PayableEntity.
     * Nota: Campos calculados (totalAmountPaid, balanceDue) e a lista de paymentTransactions
     * do DTO são ignorados durante esta conversão, pois são gerenciados pela lógica do serviço/entidade.
     *
     * @param dto The PayableDTO (with UUID ID) to convert. / O PayableDTO (com ID UUID) para converter.
     * @return The corresponding PayableEntity (with UUID ID). / A PayableEntity (com ID UUID) correspondente.
     */
    public PayableEntity dtoToEntity(PayableDTO dto) {
        if (dto == null) {
            return null;
        }
        // Note: Do NOT map paymentTransactions from DTO to Entity here. Transactions are added via service.
        // Nota: NÃO mapeie paymentTransactions do DTO para a Entidade aqui. Transações são adicionadas via serviço.
        return PayableEntity.builder()
                .id(dto.getId()) // <<<--- UUID (Keep ID for updates)
                .supplierId(dto.getSupplierId())
                .projectId(dto.getProjectId())
                .costCenterId(dto.getCostCenterId())
                .description(dto.getDescription())
                .invoiceReference(dto.getInvoiceReference())
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                // paymentDate and amountPaid removed / paymentDate e amountPaid removidos
                .amountDue(dto.getAmountDue())
                // Status might be recalculated on save/update based on actual transactions
                // O status pode ser recalculado ao salvar/atualizar com base nas transações reais
                .status(dto.getStatus() != null ? dto.getStatus() : null) // Allow setting initial status if provided
                .documentReferences(dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>())
                // Initialize paymentTransactions list in entity, but don't populate from DTO
                .paymentTransactions(new ArrayList<>())
                .build();
    }
}
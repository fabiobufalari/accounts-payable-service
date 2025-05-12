package com.bufalari.payable.converter;

import com.bufalari.payable.dto.PaymentTransactionDTO;
import com.bufalari.payable.entity.PaymentTransactionEntity;
import org.springframework.stereotype.Component; // Ensure @Component is present

import java.util.ArrayList;
// UUID import not needed here as it's only used via getters/setters

/**
 * Converts between PaymentTransactionEntity (with UUID ID) and PaymentTransactionDTO (with UUID ID).
 * Handles the mapping of fields between the database entity and the data transfer object.
 * Converte entre PaymentTransactionEntity (com ID UUID) e PaymentTransactionDTO (com ID UUID).
 * Trata o mapeamento de campos entre a entidade do banco de dados e o objeto de transferência de dados.
 */
@Component // <<<--- ADICIONAR @Component PARA SER UM BEAN GERENCIADO PELO SPRING
public class PaymentTransactionConverter {

    /**
     * Converts a PaymentTransactionEntity to its corresponding PaymentTransactionDTO.
     * Converte uma entidade PaymentTransactionEntity para seu DTO PaymentTransactionDTO correspondente.
     *
     * @param entity The entity object (with UUID ID) retrieved from the database. / O objeto entidade (com ID UUID) recuperado do banco de dados.
     * @return The corresponding DTO (with UUID ID), or null if the entity is null. / O DTO (com ID UUID) correspondente, ou null se a entidade for nula.
     */
    public PaymentTransactionDTO entityToDTO(PaymentTransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        return PaymentTransactionDTO.builder()
                .id(entity.getId()) // <<<--- UUID
                .transactionDate(entity.getTransactionDate())
                .amountPaid(entity.getAmountPaid())
                .paymentMethod(entity.getPaymentMethod())
                .transactionReference(entity.getTransactionReference())
                .notes(entity.getNotes())
                .documentReferences(entity.getDocumentReferences() != null ? new ArrayList<>(entity.getDocumentReferences()) : new ArrayList<>())
                // payableId is not typically included here as it's known from the context (e.g., /payables/{id}/payments)
                // payableId geralmente não é incluído aqui pois é conhecido do contexto (ex: /payables/{id}/payments)
                .build();
    }

    /**
     * Converts a PaymentTransactionDTO to its corresponding PaymentTransactionEntity.
     * Note: The 'payable' relationship (linking to PayableEntity) MUST be set explicitly in the service layer
     * before saving the transaction entity.
     * Converte um DTO PaymentTransactionDTO para sua entidade PaymentTransactionEntity correspondente.
     * Nota: O relacionamento 'payable' (ligando à PayableEntity) DEVE ser definido explicitamente na camada de serviço
     * antes de salvar a entidade de transação.
     *
     * @param dto The DTO object (with UUID ID) received from the request or other sources. / O objeto DTO (com ID UUID) recebido da requisição ou outras fontes.
     * @return The corresponding entity (with UUID ID), or null if the DTO is null. / A entidade (com ID UUID) correspondente, ou null se o DTO for nulo.
     */
    public PaymentTransactionEntity dtoToEntity(PaymentTransactionDTO dto) {
        if (dto == null) {
            return null;
        }
        // The 'payable' field needs to be set by the service before saving
        // O campo 'payable' precisa ser definido pelo serviço antes de salvar
        return PaymentTransactionEntity.builder()
                .id(dto.getId()) // <<<--- UUID (Usually null for creation, non-null for potential updates (rare))
                .transactionDate(dto.getTransactionDate())
                .amountPaid(dto.getAmountPaid())
                .paymentMethod(dto.getPaymentMethod())
                .transactionReference(dto.getTransactionReference())
                .notes(dto.getNotes())
                .documentReferences(dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>())
                // payable field is NOT set here, must be set in PayableService
                // campo payable NÃO é definido aqui, deve ser definido no PayableService
                .build();
    }
}
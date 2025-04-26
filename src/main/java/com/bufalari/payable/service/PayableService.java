// Path: src/main/java/com/bufalari/payable/service/PayableService.java
package com.bufalari.payable.service;

import com.bufalari.payable.converter.PayableConverter;
import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.enums.PayableStatus; // Import enum
import com.bufalari.payable.exception.ResourceNotFoundException;
import com.bufalari.payable.repository.PayableRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // Import BigDecimal
import java.time.LocalDate; // Import LocalDate
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing Accounts Payable.
 * Camada de serviço para gerenciamento de Contas a Pagar.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PayableService {

    private static final Logger log = LoggerFactory.getLogger(PayableService.class);

    private final PayableRepository payableRepository;
    private final PayableConverter payableConverter;

    /**
     * Creates a new payable record.
     * Cria um novo registro de conta a pagar.
     * @param payableDTO DTO containing payable data. / DTO contendo dados da conta a pagar.
     * @return The created PayableDTO. / O PayableDTO criado.
     */
    public PayableDTO createPayable(PayableDTO payableDTO) {
        log.info("Creating new payable for supplier ID: {}", payableDTO.getSupplierId());
        PayableEntity entity = payableConverter.dtoToEntity(payableDTO);
        // Defaults (status, amountPaid) and validation are handled in @PrePersist
        // Padrões (status, amountPaid) e validação são tratados em @PrePersist
        PayableEntity savedEntity = payableRepository.save(entity);
        log.info("Payable created successfully with ID: {}", savedEntity.getId());
        return payableConverter.entityToDTO(savedEntity);
    }

    /**
     * Retrieves a payable by its ID.
     * Recupera uma conta a pagar pelo seu ID.
     * @param id The ID of the payable. / O ID da conta a pagar.
     * @return The found PayableDTO. / O PayableDTO encontrado.
     * @throws ResourceNotFoundException if not found. / Se não encontrado.
     */
    @Transactional(readOnly = true)
    public PayableDTO getPayableById(Long id) {
        log.debug("Fetching payable by ID: {}", id);
        return payableRepository.findById(id)
                .map(payableConverter::entityToDTO)
                .orElseThrow(() -> {
                     String msg = "Payable not found with ID: " + id;
                     log.warn(msg);
                     return new ResourceNotFoundException(msg);
                });
    }

    /**
     * Retrieves all payables (consider pagination for large datasets).
     * Recupera todas as contas a pagar (considere paginação para grandes volumes de dados).
     * @return List of all PayableDTOs. / Lista de todos os PayableDTOs.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getAllPayables() {
        log.debug("Fetching all payables.");
        return payableRepository.findAll().stream()
                .map(payableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

     /**
     * Retrieves payables by status.
     * Recupera contas a pagar por status.
     * @param status The status to filter by. / O status para filtrar.
     * @return List of PayableDTOs with the specified status. / Lista de PayableDTOs com o status especificado.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getPayablesByStatus(PayableStatus status) {
        log.debug("Fetching payables by status: {}", status);
        // Requires a method in PayableRepository: List<PayableEntity> findByStatus(PayableStatus status);
        // Requer um método no PayableRepository: List<PayableEntity> findByStatus(PayableStatus status);
        // Example implementation (add findByStatus to repository):
         List<PayableEntity> entities = payableRepository.findAll().stream()
                 .filter(p -> p.getStatus() == status)
                 .collect(Collectors.toList()); // Basic filter, prefer repository method
        return entities.stream()
                .map(payableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

     /**
     * Retrieves overdue payables (due date is past and not fully paid/canceled).
     * Recupera contas a pagar atrasadas (data de vencimento passou e não totalmente pagas/canceladas).
     * @return List of overdue PayableDTOs. / Lista de PayableDTOs atrasados.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getOverduePayables() {
        log.debug("Fetching overdue payables.");
        LocalDate today = LocalDate.now();
        List<PayableStatus> excludedStatuses = List.of(PayableStatus.PAID, PayableStatus.CANCELED);
        List<PayableEntity> overdueEntities = payableRepository.findByDueDateBeforeAndStatusNotIn(today, excludedStatuses);
        return overdueEntities.stream()
                .map(payableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing payable record.
     * Atualiza um registro de conta a pagar existente.
     * @param id The ID of the payable to update. / O ID da conta a pagar a ser atualizada.
     * @param payableDTO DTO containing updated data. / DTO contendo dados atualizados.
     * @return The updated PayableDTO. / O PayableDTO atualizado.
     * @throws ResourceNotFoundException if the payable is not found. / Se a conta a pagar não for encontrada.
     */
    public PayableDTO updatePayable(Long id, PayableDTO payableDTO) {
        log.info("Updating payable with ID: {}", id);
        PayableEntity existingPayable = payableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payable not found with ID: " + id));

        // Update fields from DTO
        existingPayable.setSupplierId(payableDTO.getSupplierId()); // Allow changing supplier? Maybe restrict.
        existingPayable.setProjectId(payableDTO.getProjectId());
        existingPayable.setCostCenterId(payableDTO.getCostCenterId());
        existingPayable.setDescription(payableDTO.getDescription());
        existingPayable.setInvoiceReference(payableDTO.getInvoiceReference());
        existingPayable.setIssueDate(payableDTO.getIssueDate());
        existingPayable.setDueDate(payableDTO.getDueDate());
        existingPayable.setPaymentDate(payableDTO.getPaymentDate());
        existingPayable.setAmountDue(payableDTO.getAmountDue());
        existingPayable.setAmountPaid(payableDTO.getAmountPaid() != null ? payableDTO.getAmountPaid() : BigDecimal.ZERO);
        existingPayable.setStatus(payableDTO.getStatus());
        existingPayable.setDocumentReferences(payableDTO.getDocumentReferences() != null ? new ArrayList<>(payableDTO.getDocumentReferences()) : new ArrayList<>());

        // Validation and defaults happen in @PreUpdate via @PrePersist logic
        PayableEntity updatedEntity = payableRepository.save(existingPayable);
        log.info("Payable updated successfully with ID: {}", id);
        return payableConverter.entityToDTO(updatedEntity);
    }

    /**
     * Partially updates the status and payment details of a payable.
     * Atualiza parcialmente o status e detalhes de pagamento de uma conta a pagar.
     * @param id The ID of the payable to update. / O ID da conta a pagar a ser atualizada.
     * @param newStatus The new status. / O novo status.
     * @param paymentDate The date of the payment (if applicable). / A data do pagamento (se aplicável).
     * @param amountPaid The amount paid in this transaction (if applicable). / O valor pago nesta transação (se aplicável).
     * @return The updated PayableDTO. / O PayableDTO atualizado.
     * @throws ResourceNotFoundException if the payable is not found. / Se a conta a pagar não for encontrada.
     */
    public PayableDTO updatePayableStatus(Long id, PayableStatus newStatus, LocalDate paymentDate, BigDecimal amountPaid) {
        log.info("Updating status for payable ID: {} to {}", id, newStatus);
        PayableEntity payable = payableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payable not found with ID: " + id));

        payable.setStatus(newStatus);
        if (paymentDate != null) {
             payable.setPaymentDate(paymentDate);
        }
        // Update amount paid - consider adding to existing amountPaid for partial payments
        if (amountPaid != null) {
             if (payable.getAmountPaid() == null) payable.setAmountPaid(BigDecimal.ZERO);
             // Decide if amountPaid replaces or adds to existing amountPaid
             // payable.setAmountPaid(payable.getAmountPaid().add(amountPaid)); // Example: Add payment
             payable.setAmountPaid(amountPaid); // Example: Set total paid amount
        }

        PayableEntity updatedEntity = payableRepository.save(payable);
        return payableConverter.entityToDTO(updatedEntity);
    }


    /**
     * Deletes a payable record by its ID.
     * Deleta um registro de conta a pagar pelo seu ID.
     * @param id The ID of the payable to delete. / O ID da conta a pagar a ser deletada.
     * @throws ResourceNotFoundException if the payable is not found. / Se a conta a pagar não for encontrada.
     */
    public void deletePayable(Long id) {
        log.info("Deleting payable with ID: {}", id);
        if (!payableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payable not found with ID: " + id);
        }
        payableRepository.deleteById(id);
        log.info("Payable deleted successfully with ID: {}", id);
    }

    // --- Methods for Financial Recovery Focus ---

     /**
     * Calculates the total amount pending for payment (excluding Canceled).
     * Calcula o valor total pendente de pagamento (excluindo Canceladas).
     * @return Total pending amount. / Valor total pendente.
     */
     @Transactional(readOnly = true)
     public BigDecimal getTotalPendingAmount() {
         List<PayableStatus> activeStatuses = List.of(PayableStatus.PENDING, PayableStatus.OVERDUE, PayableStatus.PARTIALLY_PAID, PayableStatus.IN_NEGOTIATION);
         List<PayableEntity> pendingPayables = payableRepository.findAll().stream()
                 .filter(p -> activeStatuses.contains(p.getStatus()))
                 .toList();

         return pendingPayables.stream()
                 .map(p -> p.getAmountDue().subtract(p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO))
                 .reduce(BigDecimal.ZERO, BigDecimal::add);
     }

     /**
     * Calculates the total amount overdue.
     * Calcula o valor total atrasado.
     * @return Total overdue amount. / Valor total atrasado.
     */
     @Transactional(readOnly = true)
     public BigDecimal getTotalOverdueAmount() {
         List<PayableEntity> overduePayables = getOverduePayables().stream().map(payableConverter::dtoToEntity).toList(); // Reuse existing logic
         return overduePayables.stream()
                 .map(p -> p.getAmountDue().subtract(p.getAmountPaid() != null ? p.getAmountPaid() : BigDecimal.ZERO))
                 .reduce(BigDecimal.ZERO, BigDecimal::add);
     }
}
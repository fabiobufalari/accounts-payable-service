// Path: src/main/java/com/bufalari/payable/service/PayableService.java
package com.bufalari.payable.service;

// --- Imports ---
import com.bufalari.payable.converter.PayableConverter;
import com.bufalari.payable.converter.PaymentTransactionConverter;
import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.dto.PaymentTransactionDTO;
import com.bufalari.payable.dto.PayableSummaryDTO;
import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.entity.PaymentTransactionEntity;
import com.bufalari.payable.enums.PayableStatus;
// import com.bufalari.payable.enums.PaymentMethod; // Import if needed
import com.bufalari.payable.exception.OperationNotAllowedException;
import com.bufalari.payable.exception.ResourceNotFoundException;
import com.bufalari.payable.repository.PayableRepository;
import com.bufalari.payable.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service layer for managing Accounts Payable, including payment transactions.
 * Camada de serviço para gerenciamento de Contas a Pagar, incluindo transações de pagamento.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PayableService {

    private static final Logger log = LoggerFactory.getLogger(PayableService.class);

    private final PayableRepository payableRepository;
    private final PayableConverter payableConverter;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionConverter paymentTransactionConverter;
    // private final AccountsPayableClient accountsPayableClient; // For dependency check stub

    // ========================================================================
    // --- Payable CRUD Methods ---
    // ========================================================================

    /**
     * Creates a new payable record.
     * Cria um novo registro de conta a pagar.
     * @param payableDTO DTO containing payable data.
     * @return The created PayableDTO.
     */
    public PayableDTO createPayable(PayableDTO payableDTO) {
        log.info("Creating new payable for supplier ID: {}", payableDTO.getSupplierId());
        if (payableDTO.getSupplierId() == null) {
            throw new IllegalArgumentException("Supplier ID is required to create a payable.");
        }
        PayableEntity entity = payableConverter.dtoToEntity(payableDTO);
        // Status is set in @PrePersist within the entity
        PayableEntity savedEntity = payableRepository.save(entity);
        log.info("Payable created successfully with ID: {}", savedEntity.getId());
        return payableConverter.entityToDTO(savedEntity);
    }

    /**
     * Retrieves a payable by its ID, including its payment transactions.
     * Recupera uma conta a pagar pelo seu ID, incluindo suas transações de pagamento.
     * @param id The ID of the payable.
     * @return The found PayableDTO.
     * @throws ResourceNotFoundException if not found.
     */
    @Transactional(readOnly = true)
    public PayableDTO getPayableById(Long id) {
        log.debug("Fetching payable by ID: {}", id);
        PayableEntity entity = payableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payable not found with ID: " + id));
        // Ensure transactions are loaded if lazy (OSIV usually handles this, but explicit call is safer)
        // Garante que as transações sejam carregadas se forem lazy (OSIV geralmente lida com isso, mas chamada explícita é mais segura)
        // entity.getPaymentTransactions().size(); // Uncomment if you face LazyInitializationException
        return payableConverter.entityToDTO(entity); // Converter handles mapping transactions
    }

    /**
     * Retrieves all payables. Consider pagination for large datasets.
     * Recupera todas as contas a pagar. Considere paginação para grandes volumes de dados.
     * Note: Depending on fetch strategy, this might load all transactions too.
     * Nota: Dependendo da estratégia de fetch, isso pode carregar todas as transações também.
     * @return List of all PayableDTOs.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getAllPayables() {
        log.debug("Fetching all payables.");
        return payableRepository.findAll().stream()
                .map(payableConverter::entityToDTO) // Converter maps transactions
                .collect(Collectors.toList());
    }

     /**
     * Retrieves payables filtered by status.
     * Recupera contas a pagar filtradas por status.
     * @param status The status to filter by.
     * @return List of PayableDTOs with the specified status.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getPayablesByStatus(PayableStatus status) {
        log.debug("Fetching payables by status: {}", status);
        List<PayableEntity> entities = payableRepository.findByStatus(status); // Use repository method
        return entities.stream()
                .map(payableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves overdue payables (due date passed and not fully settled).
     * Recupera contas a pagar atrasadas (data de vencimento passou e não totalmente quitadas).
     * @return List of overdue PayableDTOs.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getOverduePayables() {
        log.debug("Fetching overdue payables.");
        LocalDate today = LocalDate.now();
        // Find payables due before today that are not PAID or CANCELED
        // Encontra contas a pagar vencidas antes de hoje que não estão PAGAS ou CANCELADAS
        List<PayableStatus> settledStatuses = List.of(PayableStatus.PAID, PayableStatus.CANCELED);
        List<PayableEntity> overdueEntities = payableRepository.findByDueDateBeforeAndStatusNotIn(today, settledStatuses);
        return overdueEntities.stream()
                .map(payableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates core information of an existing payable record.
     * Does not directly update payment details or status (use registerPaymentTransaction for that).
     * Atualiza informações centrais de um registro de conta a pagar existente.
     * Não atualiza diretamente detalhes de pagamento ou status (use registerPaymentTransaction para isso).
     * @param id The ID of the payable to update.
     * @param payableDTO DTO containing updated data (paymentDate, amountPaid, status in DTO are ignored).
     * @return The updated PayableDTO.
     * @throws ResourceNotFoundException if the payable is not found.
     */
    public PayableDTO updatePayable(Long id, PayableDTO payableDTO) {
        log.info("Updating payable core info for ID: {}", id);
        PayableEntity existingPayable = payableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payable not found with ID: " + id));

        // Update only non-payment related fields from DTO
        existingPayable.setSupplierId(payableDTO.getSupplierId());
        existingPayable.setProjectId(payableDTO.getProjectId());
        existingPayable.setCostCenterId(payableDTO.getCostCenterId());
        existingPayable.setDescription(payableDTO.getDescription());
        existingPayable.setInvoiceReference(payableDTO.getInvoiceReference());
        existingPayable.setIssueDate(payableDTO.getIssueDate());
        existingPayable.setDueDate(payableDTO.getDueDate());
        existingPayable.setAmountDue(payableDTO.getAmountDue());
        // Document references can be updated here if needed, or via specific endpoint
        // existingPayable.setDocumentReferences(...);

        // Recalculate status based on existing payments in case AmountDue changed
        // Recalcula o status baseado nos pagamentos existentes caso AmountDue tenha mudado
        existingPayable.updateStatusBasedOnPayments();

        PayableEntity updatedEntity = payableRepository.save(existingPayable);
        log.info("Payable core info updated successfully for ID: {}", id);
        return payableConverter.entityToDTO(updatedEntity);
    }

    /**
     * Deletes a payable record and its associated payment transactions (due to CascadeType.ALL).
     * Deleta um registro de conta a pagar e suas transações de pagamento associadas (devido ao CascadeType.ALL).
     * @param id The ID of the payable to delete.
     * @throws ResourceNotFoundException if the payable is not found.
     */
    public void deletePayable(Long id) {
        log.info("Attempting to delete payable with ID: {}", id);
        if (!payableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payable not found with ID: " + id);
        }
        payableRepository.deleteById(id); // Cascade will delete transactions
        log.info("Payable deleted successfully with ID: {}", id);
    }

    // ========================================================================
    // --- Payment Transaction Management Methods ---
    // ========================================================================

    /**
     * Registers a new payment transaction for a specific payable and updates the payable's status.
     * Registra uma nova transação de pagamento para uma conta a pagar específica e atualiza o status da conta a pagar.
     */
    public PaymentTransactionDTO registerPaymentTransaction(Long payableId, PaymentTransactionDTO transactionDTO) {
        log.info("Registering payment transaction for payable ID: {}", payableId);
        PayableEntity payable = payableRepository.findById(payableId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot register payment: Payable not found with ID: " + payableId));

        if (payable.getStatus() == PayableStatus.CANCELED) {
             throw new OperationNotAllowedException("Cannot register payment for a CANCELED payable.");
        }
        // Consider if payments should be allowed on PAID items (e.g., refunds?)
        // if (payable.getStatus() == PayableStatus.PAID) { ... }

        PaymentTransactionEntity transactionEntity = paymentTransactionConverter.dtoToEntity(transactionDTO);
        transactionEntity.setPayable(payable); // Link to parent

        // --- Handle Document Stub ---
        // TODO: Implement actual document saving logic here if needed for transactions
        if (transactionDTO.getDocumentReferences() != null && !transactionDTO.getDocumentReferences().isEmpty()) {
            // String savedDocRef = documentStorageService.save(transactionDTO.getDocumentFile()); // Example
            // transactionEntity.setDocumentReferences(List.of(savedDocRef));
             transactionEntity.setDocumentReferences(new ArrayList<>(transactionDTO.getDocumentReferences())); // Copying refs for now
        }
        // --- End Document Stub ---


        PaymentTransactionEntity savedTransaction = paymentTransactionRepository.save(transactionEntity);
        log.info("Payment transaction saved with ID: {}", savedTransaction.getId());

        // Update the parent payable's status after adding the transaction
        // Atualiza o status da conta a pagar pai após adicionar a transação
        payable.updateStatusBasedOnPayments();
        payableRepository.save(payable); // Save the updated status on the payable
        log.info("Updated status for payable ID {} to {}", payable.getId(), payable.getStatus());

        return paymentTransactionConverter.entityToDTO(savedTransaction);
    }

    /**
     * Retrieves all payment transactions for a specific payable, ordered by transaction date descending.
     * Recupera todas as transações de pagamento para uma conta a pagar específica, ordenadas por data da transação descendente.
     */
    @Transactional(readOnly = true)
    public List<PaymentTransactionDTO> getPaymentTransactionsForPayable(Long payableId) {
        log.debug("Fetching payment transactions for payable ID: {}", payableId);
        // Check if payable exists first to give a clearer error message
        if (!payableRepository.existsById(payableId)) {
             throw new ResourceNotFoundException("Payable not found with ID: " + payableId);
        }
        List<PaymentTransactionEntity> transactions = paymentTransactionRepository.findByPayableIdOrderByTransactionDateDesc(payableId);
        return transactions.stream()
                .map(paymentTransactionConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    // Optional: Add methods to update/delete specific payment transactions if needed
    // Opcional: Adicionar métodos para atualizar/deletar transações de pagamento específicas se necessário


    // ========================================================================
    // --- Integration & Financial Overview Methods ---
    // ========================================================================

    @Transactional(readOnly = true)
    public List<PayableSummaryDTO> getPaidPayableSummariesByPaymentDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching paid payable summaries (transactions) between {} and {}", startDate, endDate);
        List<PaymentTransactionEntity> transactions = paymentTransactionRepository.findByTransactionDateBetweenOrderByTransactionDateAsc(startDate, endDate);

        return transactions.stream()
                .map(transaction -> {
                    PayableEntity payable = transaction.getPayable();
                    if (payable == null) return null; // Should not happen with proper FK
                    return new PayableSummaryDTO(
                            payable.getId(),
                            payable.getDueDate(),
                            payable.getAmountDue(),
                            transaction.getAmountPaid(), // Amount of this specific transaction
                            payable.getStatus(),       // Current status of the parent payable
                            transaction.getTransactionDate() // Date this transaction occurred
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayableSummaryDTO> getPendingPayableSummariesByDueDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching pending payable summaries due between {} and {}", startDate, endDate);
        Collection<PayableStatus> pendingStatuses = List.of(
                PayableStatus.PENDING, PayableStatus.OVERDUE,
                PayableStatus.PARTIALLY_PAID, PayableStatus.IN_NEGOTIATION
        );
        List<PayableEntity> pendingEntities = payableRepository.findByDueDateBetweenAndStatusIn(startDate, endDate, pendingStatuses);

        return pendingEntities.stream()
                .map(entity -> new PayableSummaryDTO(
                        entity.getId(),
                        entity.getDueDate(),
                        entity.getAmountDue(),
                        entity.getTotalAmountPaid(), // Use helper method for total paid so far
                        entity.getStatus(),
                        null // No specific payment date for pending items
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
     public boolean hasActivePayablesForSupplier(Long supplierId) {
          log.debug("Checking for active payables for supplier ID: {}", supplierId);
          Collection<PayableStatus> inactiveStatuses = List.of(PayableStatus.PAID, PayableStatus.CANCELED);
          boolean exists = payableRepository.existsBySupplierIdAndStatusNotIn(supplierId, inactiveStatuses);
          log.debug("Active payables exist for supplier ID {}: {}", supplierId, exists);
          return exists;
     }

    @Transactional(readOnly = true)
     public BigDecimal getTotalPendingAmount() {
         log.debug("Calculating total pending payable amount.");
         Collection<PayableStatus> activeStatuses = List.of(PayableStatus.PENDING, PayableStatus.OVERDUE, PayableStatus.PARTIALLY_PAID, PayableStatus.IN_NEGOTIATION);
         // Optimize: Avoid loading full entities if possible
         // You could add a repository method:
         // @Query("SELECT p FROM PayableEntity p WHERE p.status IN :statuses")
         // List<PayableEntity> findByStatusIn(@Param("statuses") Collection<PayableStatus> statuses);
         // OR even better, calculate sum directly in DB:
         // @Query("SELECT SUM(p.amountDue - COALESCE((SELECT SUM(pt.amountPaid) FROM PaymentTransactionEntity pt WHERE pt.payable = p), 0)) FROM PayableEntity p WHERE p.status IN :statuses")
         // BigDecimal sumBalanceDueByStatusIn(@Param("statuses") Collection<PayableStatus> statuses);

         // Current implementation (loads entities):
         List<PayableEntity> pendingPayables = payableRepository.findAll().stream()
                 .filter(p -> activeStatuses.contains(p.getStatus()))
                 .toList();

         BigDecimal totalPending = pendingPayables.stream()
                 .map(PayableEntity::getBalanceDue) // Use entity's helper method
                 .reduce(BigDecimal.ZERO, BigDecimal::add);
         log.debug("Total pending payable amount calculated: {}", totalPending);
         return totalPending;
     }

    @Transactional(readOnly = true)
     public BigDecimal getTotalOverdueAmount() {
         log.debug("Calculating total overdue payable amount.");
         LocalDate today = LocalDate.now();
         Collection<PayableStatus> unsettledStatuses = List.of(PayableStatus.PENDING, PayableStatus.OVERDUE, PayableStatus.PARTIALLY_PAID, PayableStatus.IN_NEGOTIATION);
         // Optimize: Similar to getTotalPendingAmount, consider a direct DB sum query
         // @Query("SELECT SUM(p.amountDue - COALESCE((SELECT SUM(pt.amountPaid) FROM PaymentTransactionEntity pt WHERE pt.payable = p), 0)) FROM PayableEntity p WHERE p.dueDate < :today AND p.status IN :unsettledStatuses")
         // BigDecimal sumBalanceDueForOverdue(@Param("today") LocalDate today, @Param("unsettledStatuses") Collection<PayableStatus> unsettledStatuses);

         // Current implementation (loads entities):
         List<PayableEntity> overdueEntities = payableRepository.findByDueDateBeforeAndStatusNotIn(today, List.of(PayableStatus.PAID, PayableStatus.CANCELED));

         BigDecimal totalOverdue = overdueEntities.stream()
                 .filter(p -> unsettledStatuses.contains(p.getStatus())) // Ensure it's not PAID/CANCELED (already filtered) but also PENDING etc.
                 .map(PayableEntity::getBalanceDue) // Use entity's helper method
                 .reduce(BigDecimal.ZERO, BigDecimal::add);
         log.debug("Total overdue payable amount calculated: {}", totalOverdue);
         return totalOverdue;
     }

    // --- STUB Methods for Document Management ---
    // (Mantendo os stubs como antes)
       public String addPayableDocumentReference(Long payableId, MultipartFile file) { /* ... stub ... */ return "";}
       @Transactional(readOnly = true)
       public List<String> getPayableDocumentReferences(Long payableId) { /* ... stub ... */ return new ArrayList<>(); }
       public void deletePayableDocumentReference(Long payableId, String documentReference) { /* ... stub ... */ }
}
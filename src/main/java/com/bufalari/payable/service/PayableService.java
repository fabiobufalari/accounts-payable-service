package com.bufalari.payable.service;

import com.bufalari.payable.converter.PayableConverter;
import com.bufalari.payable.converter.PaymentTransactionConverter;
import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.dto.PaymentTransactionDTO;
import com.bufalari.payable.dto.PayableSummaryDTO;
import com.bufalari.payable.entity.PayableEntity;
import com.bufalari.payable.entity.PaymentTransactionEntity;
import com.bufalari.payable.enums.PayableStatus;
import com.bufalari.payable.exception.OperationNotAllowedException;
import com.bufalari.payable.exception.ResourceNotFoundException;
import com.bufalari.payable.repository.PayableRepository;
import com.bufalari.payable.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // Keep for stub signature

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
// Removed Comparator import as it's not used
import java.util.List;
import java.util.Objects;
import java.util.UUID; // <<<--- IMPORT UUID
import java.util.stream.Collectors;

/**
 * Service layer for managing Accounts Payable (with UUID IDs), including payment transactions.
 * Contains business logic, interacts with repositories, and uses converters.
 * Camada de serviço para gerenciamento de Contas a Pagar (com IDs UUID), incluindo transações de pagamento.
 * Contém lógica de negócio, interage com repositórios e usa conversores.
 */
@Service
@RequiredArgsConstructor // Injects final fields via constructor
@Transactional // Default transaction propagation (REQUIRED)
public class PayableService {

    private static final Logger log = LoggerFactory.getLogger(PayableService.class);

    // Repositories
    private final PayableRepository payableRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    // Converters
    private final PayableConverter payableConverter;
    private final PaymentTransactionConverter paymentTransactionConverter;

    // ========================================================================
    // --- Payable CRUD Methods (using UUID) ---
    // ========================================================================

    /**
     * Creates a new payable record.
     * The initial status is determined by the entity's @PrePersist logic.
     * Cria um novo registro de conta a pagar.
     * O status inicial é determinado pela lógica @PrePersist da entidade.
     *
     * @param payableDTO DTO containing payable data (ID should be null).
     * @return The created PayableDTO with its generated UUID.
     */
    public PayableDTO createPayable(PayableDTO payableDTO) {
        log.info("Attempting to create new payable for supplier ID: {}", payableDTO.getSupplierId());
        if (payableDTO.getId() != null) {
            log.warn("Attempted to create a payable with an existing ID ({}). ID will be ignored.", payableDTO.getId());
            payableDTO.setId(null); // Ensure ID is null for creation
        }
        if (payableDTO.getSupplierId() == null) {
            log.error("Payable creation failed: Supplier ID is required.");
            throw new IllegalArgumentException("Supplier ID is required to create a payable.");
        }

        PayableEntity entity = payableConverter.dtoToEntity(payableDTO);
        // Entity's @PrePersist handles initial status and project/cost center validation
        PayableEntity savedEntity = payableRepository.save(entity);
        log.info("Payable created successfully with ID: {}", savedEntity.getId());
        return payableConverter.entityToDTO(savedEntity);
    }

    /**
     * Retrieves a payable by its UUID, including its payment transactions.
     * Recupera uma conta a pagar pelo seu UUID, incluindo suas transações de pagamento.
     *
     * @param id The UUID of the payable.
     * @return The found PayableDTO.
     * @throws ResourceNotFoundException if no payable with the given UUID is found.
     */
    @Transactional(readOnly = true) // Read-only transaction for queries
    public PayableDTO getPayableById(UUID id) { // <<<--- UUID
        log.debug("Fetching payable by ID: {}", id);
        PayableEntity entity = payableRepository.findById(id) // Use findById with UUID
                .orElseThrow(() -> {
                    log.warn("Payable not found with ID: {}", id);
                    return new ResourceNotFoundException("Payable not found with ID: " + id);
                });

        // Converter now handles lazy loading or eager fetching based on entity definition
        // and populates the DTO including transactions.
        return payableConverter.entityToDTO(entity);
    }

    /**
     * Retrieves all payables.
     * WARNING: This can be inefficient for large datasets as it loads all entities.
     * Consider pagination or projection for production use.
     * Recupera todas as contas a pagar.
     * ATENÇÃO: Pode ser ineficiente para grandes volumes de dados. Considere paginação ou projeção.
     *
     * @return List of all PayableDTOs.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getAllPayables() {
        log.debug("Fetching all payables.");
        List<PayableEntity> entities = payableRepository.findAll();
        log.info("Retrieved {} payable entities.", entities.size());
        return entities.stream()
                .map(payableConverter::entityToDTO) // Converter maps transactions if fetched
                .collect(Collectors.toList());
        // For better performance with many payables, consider:
        // 1. Pagination: Use PagingAndSortingRepository<PayableEntity, UUID> and pass Pageable object.
        // 2. Projection: Create a DTO/Interface projection in the repository to select only needed fields.
    }

    /**
     * Retrieves payables filtered by a specific status.
     * Recupera contas a pagar filtradas por status.
     *
     * @param status The status to filter by.
     * @return List of PayableDTOs with the specified status.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getPayablesByStatus(PayableStatus status) {
        log.debug("Fetching payables by status: {}", status);
        List<PayableEntity> entities = payableRepository.findByStatus(status); // Use specific repository method
        log.info("Retrieved {} payable entities with status {}", entities.size(), status);
        return entities.stream()
                .map(payableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves overdue payables (due date passed and status is not PAID or CANCELED).
     * Recupera contas a pagar atrasadas (data de vencimento passou e status não é PAGO ou CANCELADO).
     *
     * @return List of overdue PayableDTOs.
     */
    @Transactional(readOnly = true)
    public List<PayableDTO> getOverduePayables() {
        log.debug("Fetching overdue payables.");
        LocalDate today = LocalDate.now();
        // Find payables due before today that are not in a settled state
        List<PayableStatus> settledStatuses = List.of(PayableStatus.PAID, PayableStatus.CANCELED);
        List<PayableEntity> overdueEntities = payableRepository.findByDueDateBeforeAndStatusNotIn(today, settledStatuses);
        log.info("Retrieved {} overdue payable entities.", overdueEntities.size());
        return overdueEntities.stream()
                // Double-check the status just in case (should be redundant with query but safe)
                .filter(p -> p.getStatus() != PayableStatus.PAID && p.getStatus() != PayableStatus.CANCELED)
                .map(payableConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates core information of an existing payable record identified by UUID.
     * Payment details (transactions) and status are managed separately via registerPaymentTransaction.
     * This method *will* recalculate the status based on existing payments if amountDue changes.
     * Atualiza informações centrais de um registro de conta a pagar existente identificado por UUID.
     * Detalhes de pagamento (transações) e status são gerenciados separadamente via registerPaymentTransaction.
     * Este método *irá* recalcular o status com base nos pagamentos existentes se amountDue for alterado.
     *
     * @param id The UUID of the payable to update.
     * @param payableDTO DTO containing updated data. ID in DTO is ignored (path ID is used).
     * @return The updated PayableDTO.
     * @throws ResourceNotFoundException if the payable is not found.
     */
    public PayableDTO updatePayable(UUID id, PayableDTO payableDTO) { // <<<--- UUID
        log.info("Attempting to update payable core info for ID: {}", id);
        PayableEntity existingPayable = payableRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: Payable not found with ID: {}", id);
                    return new ResourceNotFoundException("Payable not found with ID: " + id);
                });

        // Check if trying to update a Canceled payable (usually not allowed for core fields)
        if (existingPayable.getStatus() == PayableStatus.CANCELED) {
            log.warn("Attempted to update a CANCELED payable (ID: {}). Operation may be restricted.", id);
            // Depending on business rules, you might throw OperationNotAllowedException here
            // throw new OperationNotAllowedException("Cannot update core details of a CANCELED payable.");
        }

        // Update fields from DTO
        boolean amountDueChanged = !Objects.equals(existingPayable.getAmountDue(), payableDTO.getAmountDue());

        existingPayable.setSupplierId(payableDTO.getSupplierId()); // Assuming supplier can change
        existingPayable.setProjectId(payableDTO.getProjectId());
        existingPayable.setCostCenterId(payableDTO.getCostCenterId());
        existingPayable.setDescription(payableDTO.getDescription());
        existingPayable.setInvoiceReference(payableDTO.getInvoiceReference());
        existingPayable.setIssueDate(payableDTO.getIssueDate());
        existingPayable.setDueDate(payableDTO.getDueDate());
        existingPayable.setAmountDue(payableDTO.getAmountDue());

        // Document references: If managed here, update them. Otherwise handled by separate endpoints.
        // Example: existingPayable.setDocumentReferences(new ArrayList<>(payableDTO.getDocumentReferences()));

        // Entity's @PreUpdate validates project/cost center allocation

        // If amountDue changed, recalculate status based on existing payments
        if (amountDueChanged) {
            log.debug("Amount Due changed for payable ID {}. Recalculating status.", id);
            existingPayable.updateStatusBasedOnPayments();
        }
        // Also recalculate if due date changed and it might affect Overdue status
        // Note: updateStatusBasedOnPayments already considers due date for OVERDUE state.

        PayableEntity updatedEntity = payableRepository.save(existingPayable);
        log.info("Payable core info updated successfully for ID: {}", id);
        return payableConverter.entityToDTO(updatedEntity);
    }

    /**
     * Deletes a payable record identified by UUID.
     * Associated payment transactions will also be deleted due to CascadeType.ALL.
     * Deleta um registro de conta a pagar identificado por UUID.
     * Transações de pagamento associadas também serão deletadas devido ao CascadeType.ALL.
     *
     * @param id The UUID of the payable to delete.
     * @throws ResourceNotFoundException if the payable is not found.
     */
    public void deletePayable(UUID id) { // <<<--- UUID
        log.info("Attempting to delete payable with ID: {}", id);
        // Check existence before deletion to provide 404 if not found
        if (!payableRepository.existsById(id)) { // Use existsById with UUID
            log.warn("Delete failed: Payable not found with ID: {}", id);
            throw new ResourceNotFoundException("Payable not found with ID: " + id);
        }
        payableRepository.deleteById(id); // Use deleteById with UUID
        log.info("Payable deleted successfully with ID: {}", id);
    }

    // ========================================================================
    // --- Payment Transaction Management Methods (using UUID) ---
    // ========================================================================

    /**
     * Registers a new payment transaction for a specific payable (identified by UUID).
     * Updates the payable's status after the transaction is saved.
     * Registra uma nova transação de pagamento para uma conta a pagar específica (identificada por UUID).
     * Atualiza o status da conta a pagar após salvar a transação.
     *
     * @param payableId      The UUID of the payable receiving the payment.
     * @param transactionDTO DTO containing the payment transaction details.
     * @return The created PaymentTransactionDTO with its generated UUID.
     * @throws ResourceNotFoundException      if the payable is not found.
     * @throws OperationNotAllowedException if payment is attempted on a CANCELED payable.
     * @throws IllegalArgumentException    if transaction amount is not positive.
     */
    public PaymentTransactionDTO registerPaymentTransaction(UUID payableId, PaymentTransactionDTO transactionDTO) { // <<<--- UUID
        log.info("Registering payment transaction for payable ID: {}", payableId);

        // 1. Find the parent Payable entity
        PayableEntity payable = payableRepository.findById(payableId) // Use findById with UUID
                .orElseThrow(() -> {
                    log.warn("Payment registration failed: Payable not found with ID: {}", payableId);
                    return new ResourceNotFoundException("Cannot register payment: Payable not found with ID: " + payableId);
                });

        // 2. Check business rules (e.g., cannot pay canceled items)
        if (payable.getStatus() == PayableStatus.CANCELED) {
            log.warn("Payment registration rejected: Payable ID {} is CANCELED.", payableId);
            throw new OperationNotAllowedException("Cannot register payment for a CANCELED payable (ID: " + payableId + ").");
        }
        // Consider adding checks for status PAID if overpayments/refunds are not allowed/handled differently.
        // if (payable.getStatus() == PayableStatus.PAID) { ... }

        // 3. Convert DTO to Entity and link to parent
        PaymentTransactionEntity transactionEntity = paymentTransactionConverter.dtoToEntity(transactionDTO);
        transactionEntity.setPayable(payable); // Establish the relationship

        // 4. Save the transaction (Entity's @PrePersist validates positive amount)
        PaymentTransactionEntity savedTransaction;
        try {
            savedTransaction = paymentTransactionRepository.save(transactionEntity);
            log.info("Payment transaction saved with ID: {} for payable ID: {}", savedTransaction.getId(), payableId);
        } catch (Exception e) { // Catch persistence exceptions (like validation constraint from PrePersist)
            log.error("Failed to save payment transaction for payable ID {}: {}", payableId, e.getMessage(), e);
            // Re-throw as a more specific exception or handle appropriately
            throw new RuntimeException("Failed to save payment transaction: " + e.getMessage(), e);
        }


        // --- Document Stub Integration (If needed for transactions) ---
        // if (transactionDTO.getDocumentReferences() != null && !transactionDTO.getDocumentReferences().isEmpty()) {
        // Persist document references associated *specifically* with the payment transaction
        // savedTransaction.setDocumentReferences(new ArrayList<>(transactionDTO.getDocumentReferences()));
        // paymentTransactionRepository.save(savedTransaction); // Save again if references modified after initial save
        // log.debug("Associated document references with payment transaction ID: {}", savedTransaction.getId());
        // }
        // --- End Document Stub ---


        // 5. Update the parent payable's status based on ALL its payments
        payable.updateStatusBasedOnPayments(); // Call the entity's helper method
        payableRepository.save(payable); // Save the updated status on the payable
        log.info("Updated status for payable ID {} to {}", payable.getId(), payable.getStatus());

        // 6. Convert saved transaction entity back to DTO for response
        return paymentTransactionConverter.entityToDTO(savedTransaction);
    }

    /**
     * Retrieves all payment transactions for a specific payable (identified by UUID),
     * ordered by transaction date descending.
     * Recupera todas as transações de pagamento para uma conta a pagar específica (identificada por UUID),
     * ordenadas por data da transação descendente.
     *
     * @param payableId The UUID of the payable.
     * @return A list of PaymentTransactionDTOs.
     * @throws ResourceNotFoundException if the payable itself is not found.
     */
    @Transactional(readOnly = true)
    public List<PaymentTransactionDTO> getPaymentTransactionsForPayable(UUID payableId) { // <<<--- UUID
        log.debug("Fetching payment transactions for payable ID: {}", payableId);
        // Check if payable exists first to provide a better error context if needed
        if (!payableRepository.existsById(payableId)) { // Use existsById with UUID
            log.warn("Cannot get payments: Payable not found with ID: {}", payableId);
            throw new ResourceNotFoundException("Payable not found with ID: " + payableId + ", cannot retrieve payments.");
        }

        // Fetch transactions using the repository method with UUID
        List<PaymentTransactionEntity> transactions = paymentTransactionRepository.findByPayableIdOrderByTransactionDateDesc(payableId);
        log.info("Retrieved {} payment transactions for payable ID: {}", transactions.size(), payableId);

        return transactions.stream()
                .map(paymentTransactionConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    // Optional: Add methods to get, update, or delete a *specific* payment transaction by its UUID if required.
    // Example:
    // public PaymentTransactionDTO getPaymentTransactionById(UUID transactionId) { ... }
    // public void deletePaymentTransaction(UUID transactionId) { ... payable.updateStatusBasedOnPayments(); ... }


    // ========================================================================
    // --- Integration & Financial Overview Methods (using UUIDs where appropriate) ---
    // ========================================================================

    /**
     * Retrieves summaries of payables based on *payment transactions* occurring within a date range.
     * Used for cash flow analysis (outflows). Returns one summary DTO per *transaction*.
     * Recupera resumos de contas a pagar com base em *transações de pagamento* ocorrendo em um intervalo de datas.
     * Usado para análise de fluxo de caixa (saídas). Retorna um DTO de resumo por *transação*.
     *
     * @param startDate Start date of the transaction date range (inclusive).
     * @param endDate   End date of the transaction date range (inclusive).
     * @return List of PayableSummaryDTOs, each representing a payment transaction.
     */
    @Transactional(readOnly = true)
    public List<PayableSummaryDTO> getPaidPayableSummariesByPaymentDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching paid payable summaries (based on transaction dates) between {} and {}", startDate, endDate);
        List<PaymentTransactionEntity> transactions = paymentTransactionRepository.findByTransactionDateBetweenOrderByTransactionDateAsc(startDate, endDate);
        log.info("Found {} payment transactions between {} and {}", transactions.size(), startDate, endDate);

        return transactions.stream()
                .map(transaction -> {
                    PayableEntity payable = transaction.getPayable();
                    // Basic check in case of data inconsistency (should have payable via FK)
                    if (payable == null) {
                        log.error("Data integrity issue: Payment Transaction ID {} has no associated PayableEntity.", transaction.getId());
                        return null;
                    }
                    // Create summary DTO using data from both transaction and payable
                    return new PayableSummaryDTO(
                            payable.getId(),                // UUID of the parent payable
                            payable.getDueDate(),           // Due date of the parent payable
                            payable.getAmountDue(),         // Original amount due of the parent payable
                            transaction.getAmountPaid(),    // Amount paid in THIS transaction
                            payable.getStatus(),            // Current status of the parent payable (at time of query)
                            transaction.getTransactionDate()// Date THIS transaction occurred
                    );
                })
                .filter(Objects::nonNull) // Filter out nulls from potential data issues
                .collect(Collectors.toList());
    }

    /**
     * Retrieves summaries of *pending* (not fully paid or canceled) payables that are *due* within a date range.
     * Used for forecasting upcoming payment obligations. Returns one summary DTO per *payable*.
     * Recupera resumos de contas a pagar *pendentes* (não totalmente pagas ou canceladas) que *vencem* em um intervalo de datas.
     * Usado para prever obrigações de pagamento futuras. Retorna um DTO de resumo por *conta a pagar*.
     *
     * @param startDate Start date of the due date range (inclusive).
     * @param endDate   End date of the due date range (inclusive).
     * @return List of PayableSummaryDTOs, each representing a pending payable.
     */
    @Transactional(readOnly = true)
    public List<PayableSummaryDTO> getPendingPayableSummariesByDueDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching pending payable summaries due between {} and {}", startDate, endDate);
        // Define statuses considered "pending" or "active"
        Collection<PayableStatus> pendingStatuses = List.of(
                PayableStatus.PENDING,
                PayableStatus.OVERDUE,
                PayableStatus.PARTIALLY_PAID,
                PayableStatus.IN_NEGOTIATION
        );
        List<PayableEntity> pendingEntities = payableRepository.findByDueDateBetweenAndStatusIn(startDate, endDate, pendingStatuses);
        log.info("Found {} pending payables due between {} and {}", pendingEntities.size(), startDate, endDate);

        return pendingEntities.stream()
                .map(entity -> new PayableSummaryDTO(
                        entity.getId(),                 // UUID of the payable
                        entity.getDueDate(),            // Due date
                        entity.getAmountDue(),          // Original amount due
                        entity.getTotalAmountPaid(),    // Use helper method for total paid so far
                        entity.getStatus(),             // Current status
                        null                            // No specific *single* payment date for a pending item summary
                ))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a supplier (identified by Long ID) has any active (not PAID or CANCELED) payables.
     * Useful for validation before potentially deleting or deactivating a supplier.
     * Verifica se um fornecedor (identificado por ID Long) possui contas a pagar ativas (não PAGAS ou CANCELADAS).
     *
     * @param supplierId The Long ID of the supplier.
     * @return true if active payables exist, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean hasActivePayablesForSupplier(Long supplierId) {
        log.debug("Checking for active payables for supplier ID: {}", supplierId);
        Collection<PayableStatus> inactiveStatuses = List.of(PayableStatus.PAID, PayableStatus.CANCELED);
        // Use the specific repository query for efficiency
        boolean exists = payableRepository.existsBySupplierIdAndStatusNotIn(supplierId, inactiveStatuses);
        log.debug("Active payables exist for supplier ID {}: {}", supplierId, exists);
        return exists;
    }

    /**
     * Calculates the total remaining balance due across all active payables.
     * Active statuses include PENDING, OVERDUE, PARTIALLY_PAID, IN_NEGOTIATION.
     * Calcula o saldo devedor total restante em todas as contas a pagar ativas.
     *
     * @return The sum of balance due for all active payables.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingAmount() {
        log.debug("Calculating total pending payable amount.");
        Collection<PayableStatus> activeStatuses = List.of(
                PayableStatus.PENDING, PayableStatus.OVERDUE,
                PayableStatus.PARTIALLY_PAID, PayableStatus.IN_NEGOTIATION
        );

        // --- Optimization Note ---
        // Loading all entities just to sum balances can be slow.
        // Consider using a direct repository query for summation if performance is critical.
        // Example: payableRepository.sumBalanceDueByStatusIn(activeStatuses); (See repository comments)

        // Current implementation (loads entities):
        List<PayableEntity> activePayables = payableRepository.findAll().stream() // Inefficient: loads ALL payables first
                .filter(p -> activeStatuses.contains(p.getStatus()))
                .toList();

        BigDecimal totalPending = activePayables.stream()
                .map(PayableEntity::getBalanceDue) // Use entity's helper method
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Total pending payable amount calculated: {}", totalPending);
        return totalPending;
    }

    /**
     * Calculates the total remaining balance due specifically for overdue payables.
     * Overdue means due date is in the past AND status is not PAID or CANCELED.
     * Calcula o saldo devedor total restante especificamente para contas a pagar atrasadas.
     *
     * @return The sum of balance due for all overdue payables.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalOverdueAmount() {
        log.debug("Calculating total overdue payable amount.");
        LocalDate today = LocalDate.now();
        Collection<PayableStatus> settledStatuses = List.of(PayableStatus.PAID, PayableStatus.CANCELED);

        // --- Optimization Note ---
        // Similar to getTotalPendingAmount, consider a direct DB sum query.
        // Example: payableRepository.sumBalanceDueForOverdue(today, activeStatuses); (See repository comments)

        // Current implementation (loads entities):
        List<PayableEntity> overdueEntities = payableRepository.findByDueDateBeforeAndStatusNotIn(today, settledStatuses);

        BigDecimal totalOverdue = overdueEntities.stream()
                // No need to filter status again, repository query already excluded PAID/CANCELED
                .map(PayableEntity::getBalanceDue) // Use entity's helper method
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Total overdue payable amount calculated: {}", totalOverdue);
        return totalOverdue;
    }

    // ========================================================================
    // --- STUB Methods for Document Management (Updated Signatures) ---
    // ========================================================================
    // These are placeholders. Implement actual document storage/retrieval logic.

    /** STUB: Adds a document reference to a payable. */
    public String addPayableDocumentReference(UUID payableId, MultipartFile file) { // <<<--- UUID
        log.warn("[STUB] addPayableDocumentReference called for payableId: {}, file: {}", payableId, file.getOriginalFilename());
        // 1. Check payable exists: getPayableById(payableId);
        // 2. Store the file (e.g., S3, local storage) -> get documentReference (URL/ID)
        // 3. Find PayableEntity
        // 4. Add reference to entity.getDocumentReferences()
        // 5. payableRepository.save(entity)
        // 6. Return reference
        getPayableById(payableId); // Check existence
        return "stub-ref-" + file.getName() + "-" + System.currentTimeMillis();
    }

    /** STUB: Gets document references for a payable. */
    @Transactional(readOnly = true)
    public List<String> getPayableDocumentReferences(UUID payableId) { // <<<--- UUID
        log.warn("[STUB] getPayableDocumentReferences called for payableId: {}", payableId);
        // 1. Find PayableEntity by ID
        // 2. Return entity.getDocumentReferences()
        PayableDTO dto = getPayableById(payableId); // Check existence and get DTO
        return dto.getDocumentReferences() != null ? new ArrayList<>(dto.getDocumentReferences()) : new ArrayList<>();
    }

    /** STUB: Deletes a document reference from a payable. */
    public void deletePayableDocumentReference(UUID payableId, String documentReference) { // <<<--- UUID
        log.warn("[STUB] deletePayableDocumentReference called for payableId: {}, ref: {}", payableId, documentReference);
        // 1. Find PayableEntity by ID
        // 2. Remove reference from entity.getDocumentReferences()
        // 3. payableRepository.save(entity)
        // 4. Optionally, delete the actual file from storage
        getPayableById(payableId); // Check existence
    }
}
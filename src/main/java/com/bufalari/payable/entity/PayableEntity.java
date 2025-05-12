package com.bufalari.payable.entity;

import com.bufalari.payable.auditing.AuditableBaseEntity;
import com.bufalari.payable.enums.PayableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator; // <<<--- IMPORT for UUID generator
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Import Objects
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Represents an account payable (an amount owed to a supplier or other entity).
 * Tracks associated payment transactions. Uses UUID as primary key.
 * Representa uma conta a pagar (um valor devido a um fornecedor ou outra entidade).
 * Rastreia transações de pagamento associadas. Usa UUID como chave primária.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payables", indexes = { // Add indexes for commonly queried fields
        @Index(name = "idx_payable_supplier_id", columnList = "supplierId"),
        @Index(name = "idx_payable_status", columnList = "status"),
        @Index(name = "idx_payable_due_date", columnList = "dueDate")
})
public class PayableEntity extends AuditableBaseEntity {

    private static final Logger log = LoggerFactory.getLogger(PayableEntity.class);

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator" // Standard UUID generator
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid") // Explicitly define column type
    private UUID id; // <<<--- UUID Type

    @NotNull
    @Column(nullable = false)
    private Long supplierId; // Assuming this remains Long, referencing another domain/service

    @Column(name = "project_id")
    private Long projectId; // Assuming this remains Long

    @Column(name = "cost_center_id")
    private Long costCenterId; // Assuming this remains Long

    @NotNull
    @Column(nullable = false, length = 300)
    private String description;

    @Column(length = 100)
    private String invoiceReference;

    @NotNull
    @Column(nullable = false)
    private LocalDate issueDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate dueDate;

    // paymentDate and amountPaid REMOVED from direct fields

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountDue;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayableStatus status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "payable_document_references", joinColumns = @JoinColumn(name = "payable_id"))
    @Column(name = "document_reference")
    @Builder.Default
    private List<String> documentReferences = new ArrayList<>();

    // Cascade ALL means deleting PayableEntity deletes its PaymentTransactionEntity children
    // orphanRemoval=true means removing a transaction from this list deletes it from DB
    @OneToMany(mappedBy = "payable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentTransactionEntity> paymentTransactions = new ArrayList<>();

    // --- Helper Methods ADDED / Métodos Auxiliares ADICIONADOS ---

    /**
     * Calculates the total amount paid from all associated transactions.
     * Transient method, not persisted in the database.
     * @return Total amount paid, or BigDecimal.ZERO if no transactions or null amounts.
     */
    @Transient // Not persisted / Não persistido
    public BigDecimal getTotalAmountPaid() {
        if (paymentTransactions == null) {
            return BigDecimal.ZERO;
        }
        return paymentTransactions.stream()
                .map(PaymentTransactionEntity::getAmountPaid) // Assumes getAmountPaid() returns non-null BigDecimal
                .filter(Objects::nonNull) // Filter null amounts just in case
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the remaining balance due (amountDue - totalAmountPaid).
     * Transient method, not persisted in the database.
     * @return Balance due. Returns negative if overpaid. Returns -totalPaid if amountDue is null.
     */
    @Transient // Not persisted / Não persistido
    public BigDecimal getBalanceDue() {
        BigDecimal totalPaid = getTotalAmountPaid();
        return (amountDue != null) ? amountDue.subtract(totalPaid) : BigDecimal.ZERO.subtract(totalPaid); // Handle null amountDue
    }

    /**
     * Updates the status of the payable based on the current payment situation and due date.
     * Should be called after any change to payment transactions or amountDue.
     */
    public void updateStatusBasedOnPayments() {
        // If already canceled, status doesn't change based on payments
        if (this.status == PayableStatus.CANCELED) {
            log.trace("Status update skipped for canceled payable ID: {}", this.id);
            return;
        }

        BigDecimal balance = getBalanceDue();
        BigDecimal totalPaid = getTotalAmountPaid();
        LocalDate today = LocalDate.now();
        boolean isOverdue = this.dueDate != null && today.isAfter(this.dueDate);

        PayableStatus previousStatus = this.status; // Keep track for logging

        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) { // No payment or negative payment (refunds?)
            // If no payment, it's PENDING unless overdue
            this.status = isOverdue ? PayableStatus.OVERDUE : PayableStatus.PENDING;
        } else if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            // Paid in full or overpaid
            this.status = PayableStatus.PAID;
        } else { // balance > 0 and totalPaid > 0
            // Partially paid
            this.status = isOverdue ? PayableStatus.OVERDUE : PayableStatus.PARTIALLY_PAID;
            // Note: A partially paid item can become Overdue if the due date passes
        }

        if(previousStatus != this.status) {
            log.debug("Payable ID {} status updated from {} to {}", this.id, previousStatus, this.status);
        }
    }
    // --- End Helper Methods ---

    @PrePersist
    private void beforePersist() {
        // Set initial status if not provided
        if (status == null) {
            this.status = (dueDate != null && LocalDate.now().isAfter(dueDate)) ? PayableStatus.OVERDUE : PayableStatus.PENDING;
            log.debug("Payable ID {} initial status set to {} on persist.", this.id, this.status);
        }
        // Validation: Ensure not allocated to both project and cost center
        if (projectId != null && costCenterId != null) {
            log.error("Payable cannot be allocated to both Project ID {} and Cost Center ID {} simultaneously.", projectId, costCenterId);
            throw new IllegalStateException("Payable cannot be allocated to both a project and a cost center simultaneously.");
        }
        if (projectId == null && costCenterId == null && log.isWarnEnabled()) {
            log.warn("Payable with potential ID {} created without project or cost center allocation.", this.id); // ID might be null here before flush
        }
    }

    @PreUpdate
    private void beforeUpdate() {
        // This logic is now primarily handled by updateStatusBasedOnPayments() called explicitly in the service.
        // Recalculating here automatically on *any* update might have unintended consequences.
        // For example, changing the description shouldn't necessarily trigger a status recalculation.
        // updateStatusBasedOnPayments(); // << Consider removing this if status updates are explicit in service

        // Validation: Ensure not allocated to both project and cost center on update too
        if (projectId != null && costCenterId != null) {
            log.error("Payable update failed for ID {}: Cannot be allocated to both Project ID {} and Cost Center ID {}.", this.id, projectId, costCenterId);
            throw new IllegalStateException("Payable cannot be allocated to both a project and a cost center simultaneously.");
        }
    }

    // --- equals() and hashCode() based on ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayableEntity that = (PayableEntity) o;
        // If ID is null, objects are only equal if they are the same instance
        // If ID is not null, compare by ID
        return id != null ? id.equals(that.id) : super.equals(o);
    }

    @Override
    public int hashCode() {
        // Use ID for hashCode if not null, otherwise use default Object hashCode
        return id != null ? id.hashCode() : super.hashCode();
    }
}
// Path: src/main/java/com/bufalari/payable/entity/PayableEntity.java
package com.bufalari.payable.entity;

import com.bufalari.payable.auditing.AuditableBaseEntity;
import com.bufalari.payable.enums.PayableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Import Objects

/**
 * Represents an account payable (an amount owed to a supplier or other entity).
 * Tracks associated payment transactions.
 * Representa uma conta a pagar (um valor devido a um fornecedor ou outra entidade).
 * Rastreia transações de pagamento associadas.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payables")
public class PayableEntity extends AuditableBaseEntity {

    private static final Logger log = LoggerFactory.getLogger(PayableEntity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long supplierId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "cost_center_id")
    private Long costCenterId;

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

    @OneToMany(mappedBy = "payable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentTransactionEntity> paymentTransactions = new ArrayList<>();

    // --- Helper Methods ADDED / Métodos Auxiliares ADICIONADOS ---

    @Transient // Not persisted / Não persistido
    public BigDecimal getTotalAmountPaid() {
        if (paymentTransactions == null) {
            return BigDecimal.ZERO;
        }
        return paymentTransactions.stream()
                .map(PaymentTransactionEntity::getAmountPaid) // Assumes getAmountPaid() returns BigDecimal
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient // Not persisted / Não persistido
    public BigDecimal getBalanceDue() {
        BigDecimal totalPaid = getTotalAmountPaid();
        return (amountDue != null) ? amountDue.subtract(totalPaid) : BigDecimal.ZERO.subtract(totalPaid);
    }

    // Called internally or by service after transactions change
    // Chamado internamente ou pelo serviço após mudança nas transações
    public void updateStatusBasedOnPayments() {
        if (this.status == PayableStatus.CANCELED) {
            return;
        }
        BigDecimal balance = getBalanceDue();
        BigDecimal totalPaid = getTotalAmountPaid();
        LocalDate today = LocalDate.now();

        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) { // Treat zero or less as unpaid
            this.status = (dueDate != null && today.isAfter(dueDate)) ? PayableStatus.OVERDUE : PayableStatus.PENDING;
        } else if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = PayableStatus.PAID;
        } else { // balance > 0 and totalPaid > 0
             this.status = (dueDate != null && today.isAfter(dueDate)) ? PayableStatus.OVERDUE : PayableStatus.PARTIALLY_PAID;
        }
        // Do not log here to avoid excessive logging during calculations
    }
    // --- End Helper Methods ---

    @PrePersist
    private void setDefaults() {
        if (status == null) {
             // Initial status calculation requires transactions, which are empty on persist.
             // Set to PENDING unless due date is already passed.
             this.status = (dueDate != null && LocalDate.now().isAfter(dueDate)) ? PayableStatus.OVERDUE : PayableStatus.PENDING;
        }
        if (projectId == null && costCenterId == null) {
             log.warn("Payable created without project or cost center allocation for potential ID: {}", this.id);
        }
        if (projectId != null && costCenterId != null) {
            throw new IllegalStateException("Payable cannot be allocated to both a project and a cost center simultaneously.");
        }
    }

    @PreUpdate
    private void preUpdate() {
        // Status update should primarily happen when transactions are registered.
        // Recalculating here might be needed if amountDue changes, but can be complex.
        // For now, rely on explicit calls in the service.
        // updateStatusBasedOnPayments();
    }
}
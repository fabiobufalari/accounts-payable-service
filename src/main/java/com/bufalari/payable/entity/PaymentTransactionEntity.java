package com.bufalari.payable.entity;

import com.bufalari.payable.auditing.AuditableBaseEntity; // Assuming auditing package exists
import com.bufalari.payable.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.GenericGenerator; // <<<--- IMPORT for UUID generator

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Represents a single payment transaction made towards settling an account payable. Uses UUID as primary key.
 * Representa uma única transação de pagamento realizada para quitar uma conta a pagar. Usa UUID como chave primária.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment_transactions", indexes = { // Add index for payable FK and transaction date
        @Index(name = "idx_payment_payable_id", columnList = "payable_id"),
        @Index(name = "idx_payment_transaction_date", columnList = "transactionDate")
})
public class PaymentTransactionEntity extends AuditableBaseEntity { // Extend AuditableBaseEntity

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid") // Explicitly define column type
    private UUID id; // <<<--- UUID Type

    /**
     * The Account Payable (PayableEntity with UUID ID) this transaction is associated with.
     * A Conta a Pagar (PayableEntity com ID UUID) à qual esta transação está associada.
     */
    @NotNull // A transaction must belong to a payable
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Link back to PayableEntity
    @JoinColumn(name = "payable_id", nullable = false, // FK column name
            foreignKey = @ForeignKey(name = "fk_payment_transaction_payable")) // Optional: Define FK constraint name
    private PayableEntity payable;

    /**
     * The date the payment transaction occurred.
     * A data em que a transação de pagamento ocorreu.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate transactionDate;

    /**
     * The amount paid in this specific transaction.
     * O valor pago nesta transação específica. Must be positive.
     */
    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid;

    /**
     * The method used for this payment transaction.
     * O método utilizado para esta transação de pagamento.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30) // Adjust length if needed
    private PaymentMethod paymentMethod;

    /**
     * Reference number for the transaction (e.g., check number, e-transfer ID, last 4 digits of card). Optional.
     * Número de referência da transação (ex: número do cheque, ID da e-transfer, últimos 4 dígitos do cartão). Opcional.
     */
    @Column(length = 100)
    private String transactionReference;

    /**
     * Notes or comments specific to this transaction. Optional.
     * Notas ou comentários específicos desta transação. Opcional.
     */
    @Column(length = 500)
    private String notes;

    /**
     * References to proof of payment documents (receipts, bank statements). Optional.
     * Referências a documentos de comprovante de pagamento (recibos, extratos bancários). Opcional.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "payment_transaction_docs",
            joinColumns = @JoinColumn(name = "transaction_id"),
            foreignKey = @ForeignKey(name = "fk_payment_doc_transaction")) // Optional: Define FK constraint name
    @Column(name = "document_reference")
    @Builder.Default // Initialize list
    private List<String> documentReferences = new ArrayList<>();

    // --- equals() and hashCode() based on ID ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentTransactionEntity that = (PaymentTransactionEntity) o;
        return id != null ? id.equals(that.id) : super.equals(o);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }

    @PrePersist
    @PreUpdate
    private void validateAmountPaid() {
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            // log.error("Payment transaction amount must be positive. Found: {} for potential ID {}", amountPaid, this.id); // Log removed for brevity
            throw new IllegalArgumentException("Payment transaction amount must be positive.");
        }
    }
}
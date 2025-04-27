// Path: src/main/java/com/bufalari/payable/entity/PaymentTransactionEntity.java
package com.bufalari.payable.entity;

import com.bufalari.payable.auditing.AuditableBaseEntity; // Assuming auditing package exists

import com.bufalari.payable.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single payment transaction made towards settling an account payable.
 * Representa uma única transação de pagamento realizada para quitar uma conta a pagar.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment_transactions") // Define table name
public class PaymentTransactionEntity extends AuditableBaseEntity { // Extend AuditableBaseEntity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The Account Payable this transaction is associated with.
     * A Conta a Pagar à qual esta transação está associada.
     */
    @NotNull // A transaction must belong to a payable
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Link back to PayableEntity
    @JoinColumn(name = "payable_id", nullable = false) // Foreign key column
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
     * O valor pago nesta transação específica.
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
     * Reference number for the transaction (e.g., check number, e-transfer ID, last 4 digits of card).
     * Número de referência da transação (ex: número do cheque, ID da e-transfer, últimos 4 dígitos do cartão).
     */
    @Column(length = 100)
    private String transactionReference;

    /**
     * Notes or comments specific to this transaction.
     * Notas ou comentários específicos desta transação.
     */
    @Column(length = 500)
    private String notes;

    /**
     * References to proof of payment documents (receipts, bank statements).
     * Referências a documentos de comprovante de pagamento (recibos, extratos bancários).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "payment_transaction_docs", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "document_reference")
    @Builder.Default // Initialize list
    private List<String> documentReferences = new ArrayList<>();
}
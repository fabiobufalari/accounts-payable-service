// Path: src/main/java/com/bufalari/payable/entity/PayableEntity.java
package com.bufalari.payable.entity;

import com.bufalari.payable.auditing.AuditableBaseEntity; // Import base entity
import com.bufalari.payable.enums.PayableStatus;      // Import enum
import jakarta.persistence.*; // Standard JPA imports
import jakarta.validation.constraints.NotNull; // Use jakarta validation
import lombok.*; // Standard Lombok imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList; // Import for list initialization
import java.util.List;

/**
 * Represents an account payable (an amount owed to a supplier or other entity).
 * Representa uma conta a pagar (um valor devido a um fornecedor ou outra entidade).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payables") // Table name / Nome da tabela
public class PayableEntity extends AuditableBaseEntity { // Inherit auditing fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier of the supplier this payable is owed to.
     * Links to the supplier-service (stores the supplier's primary key).
     * Identificador do fornecedor a quem esta conta a pagar é devida.
     * Liga ao supplier-service (armazena a chave primária do fornecedor).
     */
    @NotNull // A payable must have a supplier
    @Column(nullable = false)
    private Long supplierId;

    /**
     * Identifier of the project this payable is allocated to. Null if not project-specific.
     * Links to the project-management-service.
     * Identificador do projeto ao qual esta conta a pagar é alocada. Nulo se não for específico do projeto.
     * Liga ao project-management-service.
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * Identifier for the cost center this payable is allocated to (e.g., overhead). Null if project-specific.
     * Identificador para o centro de custo ao qual esta conta a pagar é alocada (ex: despesas gerais). Nulo se específico do projeto.
     */
    @Column(name = "cost_center_id")
    private Long costCenterId;

    /**
     * Description of the payable item or service (e.g., "Concrete Delivery Batch 123", "Office Rent - July").
     * Descrição do item ou serviço a pagar (ex: "Entrega Concreto Lote 123", "Aluguel Escritório - Julho").
     */
    @NotNull
    @Column(nullable = false, length = 300) // Increased length
    private String description;

    /**
     * Reference number for the invoice, bill, or contract related to this payable.
     * Número de referência da fatura, boleto ou contrato relacionado a esta conta a pagar.
     */
    @Column(length = 100)
    private String invoiceReference;

    /**
     * Date the payable (invoice/bill) was issued or received.
     * Data em que a conta a pagar (fatura/boleto) foi emitida ou recebida.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate issueDate;

    /**
     * Due date for the payment. Critical for cash flow management.
     * Data de vencimento do pagamento. Crítico para gerenciamento de fluxo de caixa.
     */
    @NotNull
    @Column(nullable = false)
    private LocalDate dueDate;

    /**
     * Date the payment was actually made. Null if not yet paid.
     * Data em que o pagamento foi efetivamente realizado. Nulo se ainda não pago.
     */
    @Column(nullable = true) // Payment date is null until paid
    private LocalDate paymentDate;

    /**
     * The total amount due for this payable item.
     * O valor total devido para este item a pagar.
     */
    @NotNull
    @Column(nullable = false, precision = 15, scale = 2) // Precision for monetary values
    private BigDecimal amountDue;

    /**
     * The amount actually paid so far. Can be less than amountDue for partial payments.
     * O valor efetivamente pago até o momento. Pode ser menor que amountDue para pagamentos parciais.
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal amountPaid;

    /**
     * Current status of the payable (Pending, Paid, Overdue, etc.).
     * Status atual da conta a pagar (Pendente, Paga, Atrasada, etc.).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayableStatus status;

    /**
     * List of references (e.g., IDs or URLs) to supporting documents (invoice scans, receipts)
     * stored in the document-storage-service.
     * Lista de referências (ex: IDs ou URLs) para documentos de suporte (scans de faturas, recibos)
     * armazenados no document-storage-service.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "payable_document_references", joinColumns = @JoinColumn(name = "payable_id"))
    @Column(name = "document_reference")
    @Builder.Default // Initialize list for builder
    private List<String> documentReferences = new ArrayList<>();

    // Set default values before persisting
    // Define valores padrão antes de persistir
    @PrePersist
    private void setDefaults() {
        if (status == null) {
            status = PayableStatus.PENDING;
        }
        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }
        // Basic validation for allocation
        if (projectId == null && costCenterId == null) {
             log.warn("Payable created without project or cost center allocation: ID {}", this.id); // Log if needed
            // Optional: throw new IllegalStateException("Payable must be allocated to either a project or a cost center.");
        }
        if (projectId != null && costCenterId != null) {
            throw new IllegalStateException("Payable cannot be allocated to both a project and a cost center simultaneously.");
        }
    }

     // Add static logger for @PrePersist logging
    private static final Logger log = LoggerFactory.getLogger(PayableEntity.class);
}
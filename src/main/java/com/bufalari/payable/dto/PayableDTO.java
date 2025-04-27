package com.bufalari.payable.dto;

import com.bufalari.payable.enums.PayableStatus;
import io.swagger.v3.oas.annotations.media.Schema; // Import Schema for descriptions
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Account Payable data transfer (Revised for Payment Transactions).
 * Includes calculated fields for summary views.
 * DTO para transferência de dados de Contas a Pagar (Revisado para Transações de Pagamento).
 * Inclui campos calculados para visões resumidas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayableDTO {

    @Schema(description = "Unique identifier of the payable", example = "1", readOnly = true)
    private Long id;

    @NotNull(message = "Supplier ID cannot be null / ID do Fornecedor não pode ser nulo")
    @Schema(description = "ID of the supplier this payable is owed to", example = "101")
    private Long supplierId;

    @Schema(description = "Optional ID of the project this payable relates to", example = "201")
    private Long projectId;

    @Schema(description = "Optional ID of the cost center this payable relates to", example = "301")
    private Long costCenterId;

    @NotBlank(message = "Description cannot be blank / Descrição não pode ser vazia")
    @Size(max = 300)
    @Schema(description = "Description of the payable item", example = "Concrete Delivery Batch #456")
    private String description;

    @Size(max = 100)
    @Schema(description = "Invoice or reference number", example = "INV-2024-00123")
    private String invoiceReference;

    @NotNull(message = "Issue date cannot be null / Data de emissão não pode ser nula")
    @Schema(description = "Date the payable was issued", example = "2024-07-20")
    private LocalDate issueDate;

    @NotNull(message = "Due date cannot be null / Data de vencimento não pode ser nula")
    @Schema(description = "Date the payment is due", example = "2024-08-19")
    private LocalDate dueDate;

    // paymentDate is removed from the primary DTO, represented within transactions

    @NotNull(message = "Amount due cannot be null / Valor devido não pode ser nulo")
    @DecimalMin(value = "0.01", message = "Amount due must be positive / Valor devido deve ser positivo") // Changed to 0.01 if zero is invalid
    @Schema(description = "Total amount due for this payable", example = "1500.75")
    private BigDecimal amountDue;

    // amountPaid is removed

    // --- CAMPOS CALCULADOS ADICIONADOS / ADDED CALCULATED FIELDS ---
    @Schema(description = "Total amount paid across all transactions for this payable", example = "1000.00", readOnly = true)
    private BigDecimal totalAmountPaid; // <<<--- ADICIONADO / ADDED

    @Schema(description = "Remaining balance due (Amount Due - Total Amount Paid)", example = "500.75", readOnly = true)
    private BigDecimal balanceDue;      // <<<--- ADICIONADO / ADDED
    // --- FIM DOS CAMPOS ADICIONADOS / END OF ADDED FIELDS ---

    @NotNull(message = "Status cannot be null / Status não pode ser nulo")
    @Schema(description = "Current status of the payable", example = "PARTIALLY_PAID")
    private PayableStatus status;

    @Schema(description = "List of document references (IDs/URLs)", readOnly = true)
    private List<String> documentReferences;

    // Optionally include payment transactions in detailed views
    // Opcionalmente incluir transações de pagamento em visões detalhadas
    @Schema(description = "List of individual payment transactions made for this payable", readOnly = true)
    private List<PaymentTransactionDTO> paymentTransactions;
}
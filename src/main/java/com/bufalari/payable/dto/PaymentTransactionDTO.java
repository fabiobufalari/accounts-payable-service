package com.bufalari.payable.dto;


import com.bufalari.payable.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * DTO for Payment Transaction data transfer. Used when registering a new payment. Uses UUID for ID.
 * DTO para transferência de dados de Transação de Pagamento. Usado ao registrar um novo pagamento. Usa UUID para ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDTO {

    @Schema(description = "Unique identifier (UUID) of the payment transaction", example = "789a1234-b56c-78d9-e012-345678901234", readOnly = true)
    private UUID id; // <<<--- UUID (Read-only in response)

    // payableId is typically provided in the path (e.g., POST /api/payables/{payableId}/payments)
    // O payableId geralmente é fornecido no path

    @NotNull(message = "Transaction date cannot be null / Data da transação não pode ser nula")
    @Schema(description = "Date the payment transaction occurred", example = "2024-08-15")
    private LocalDate transactionDate;

    @NotNull(message = "Amount paid cannot be null / Valor pago não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount paid must be positive / Valor pago deve ser positivo")
    @Schema(description = "The amount paid in this specific transaction", example = "500.00")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment method cannot be null / Método de pagamento não pode ser nulo")
    @Schema(description = "The method used for this payment", example = "BANK_TRANSFER")
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "Transaction reference max length is 100 / Referência da transação: tamanho máx 100")
    @Schema(description = "Reference number for the transaction (e.g., check number, transfer ID)", example = "TX-987654", nullable = true)
    private String transactionReference;

    @Size(max = 500, message = "Notes max length is 500 / Notas: tamanho máx 500")
    @Schema(description = "Optional notes about the transaction", example = "Partial payment as agreed", nullable = true)
    private String notes;

    // Document references are typically handled via separate upload endpoints
    @Schema(description = "List of document references (IDs/URLs) associated with this payment (if any)", readOnly = true)
    private List<String> documentReferences;
}
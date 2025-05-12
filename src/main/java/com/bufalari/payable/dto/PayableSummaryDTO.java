package com.bufalari.payable.dto;

import com.bufalari.payable.enums.PayableStatus; // Import Enum
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * Simplified DTO representing payable data needed for cash flow calculation. Uses UUID for ID.
 * DTO simplificado representando dados de contas a pagar necessários para cálculo de fluxo de caixa. Usa UUID para ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayableSummaryDTO {

    @Schema(description = "Unique identifier (UUID) of the payable", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id; // <<<--- UUID ID of the Payable / ID UUID da Conta a Pagar

    @Schema(description = "Date the payment is due", example = "2024-08-19")
    private LocalDate dueDate;

    @Schema(description = "Original total amount due for this payable", example = "1500.75")
    private BigDecimal amountDue; // Original amount due / Valor original devido

    @Schema(description = "For paid summaries: amount of the specific transaction. For pending summaries: total amount paid so far.", example = "500.00")
    private BigDecimal amountPaid; // Total paid OR amount in this transaction / Total pago OU valor nesta transação

    @Schema(description = "Current status of the payable", example = "PARTIALLY_PAID")
    private PayableStatus status;

    @Schema(description = "Date when a specific payment happened (only relevant for paid summaries based on transaction date)", example = "2024-08-15", nullable = true)
    private LocalDate paymentDate; // Date when THIS specific payment happened (if applicable) / Data que ESTE pagamento ocorreu (se aplicável)
}
// Path: src/main/java/com/bufalari/payable/dto/PayableSummaryDTO.java
package com.bufalari.payable.dto;

import com.bufalari.payable.enums.PayableStatus; // Import Enum
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simplified DTO representing payable data needed for cash flow calculation.
 * DTO simplificado representando dados de contas a pagar necessários para cálculo de fluxo de caixa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayableSummaryDTO {
    private Long id; // ID of the Payable / ID da Conta a Pagar
    private LocalDate dueDate;
    private BigDecimal amountDue; // Original amount due / Valor original devido
    private BigDecimal amountPaid; // Total paid OR amount in this transaction / Total pago OU valor nesta transação
    private PayableStatus status;
    private LocalDate paymentDate; // Date when THIS specific payment happened (if applicable) / Data que ESTE pagamento ocorreu (se aplicável)
}
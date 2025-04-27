// Path: src/main/java/com/bufalari/payable/dto/PaymentTransactionDTO.java
package com.bufalari.payable.dto;


import com.bufalari.payable.enums.PaymentMethod;
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

/**
 * DTO for Payment Transaction data transfer. Used when registering a new payment.
 * DTO para transferência de dados de Transação de Pagamento. Usado ao registrar um novo pagamento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDTO {

    private Long id; // Read-only in response

    // payableId is typically provided in the path (e.g., POST /api/payables/{payableId}/payments)
    // O payableId geralmente é fornecido no path

    @NotNull(message = "Transaction date cannot be null / Data da transação não pode ser nula")
    private LocalDate transactionDate;

    @NotNull(message = "Amount paid cannot be null / Valor pago não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount paid must be positive / Valor pago deve ser positivo")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment method cannot be null / Método de pagamento não pode ser nulo")
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "Transaction reference max length is 100 / Referência da transação: tamanho máx 100")
    private String transactionReference;

    @Size(max = 500, message = "Notes max length is 500 / Notas: tamanho máx 500")
    private String notes;

    // Document references are typically handled via separate upload endpoints
    // Referências de documentos geralmente são tratadas via endpoints de upload separados
    private List<String> documentReferences;
}
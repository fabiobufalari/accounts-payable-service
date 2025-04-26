// Path: src/main/java/com/bufalari/payable/dto/PayableDTO.java
package com.bufalari.payable.dto;

import com.bufalari.payable.enums.PayableStatus;
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
 * DTO for Account Payable data transfer.
 * DTO para transferência de dados de Contas a Pagar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayableDTO {

    private Long id; // Read-only

    @NotNull(message = "Supplier ID cannot be null / ID do Fornecedor não pode ser nulo")
    private Long supplierId;

    private Long projectId; // Optional

    private Long costCenterId; // Optional

    @NotBlank(message = "Description cannot be blank / Descrição não pode ser vazia")
    @Size(max = 300, message = "Description max length is 300 / Descrição: tamanho máx 300")
    private String description;

    @Size(max = 100, message = "Invoice reference max length is 100 / Ref. Fatura: tamanho máx 100")
    private String invoiceReference;

    @NotNull(message = "Issue date cannot be null / Data de emissão não pode ser nula")
    private LocalDate issueDate;

    @NotNull(message = "Due date cannot be null / Data de vencimento não pode ser nula")
    private LocalDate dueDate;

    private LocalDate paymentDate; // Nullable

    @NotNull(message = "Amount due cannot be null / Valor devido não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount due must be positive / Valor devido deve ser positivo")
    private BigDecimal amountDue;

    private BigDecimal amountPaid; // Nullable, defaults to 0

    @NotNull(message = "Status cannot be null / Status não pode ser nulo")
    private PayableStatus status;

    private List<String> documentReferences; // List of document IDs/URLs
}
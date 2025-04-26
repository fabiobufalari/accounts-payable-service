// Path: src/main/java/com/bufalari/payable/controller/PayableController.java
package com.bufalari.payable.controller;

import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.enums.PayableStatus;
import com.bufalari.payable.service.PayableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter; // Import Parameter
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat; // For date parsing
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize; // Import later for security
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing accounts payable.
 * Controlador REST para gerenciamento de contas a pagar.
 */
@RestController
@RequestMapping("/api/payables")
@RequiredArgsConstructor
@Tag(name = "Accounts Payable", description = "Endpoints for managing accounts payable / Endpoints para gerenciamento de contas a pagar")
// Add @SecurityRequirement(name = "bearerAuth") later
public class PayableController {

    private static final Logger log = LoggerFactory.getLogger(PayableController.class);
    private final PayableService payableService;

    /**
     * Creates a new payable record.
     * Cria um novo registro de conta a pagar.
     */
    @Operation(summary = "Create Payable", description = "Creates a new account payable record.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payable created successfully", content = @Content(schema = @Schema(implementation = PayableDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        // Add 401/403 when security is implemented
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PayableDTO> createPayable(@Valid @RequestBody PayableDTO payableDTO) {
        log.info("Received request to create payable: {}", payableDTO.getDescription());
        PayableDTO createdPayable = payableService.createPayable(payableDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPayable.getId())
                .toUri();
        log.info("Payable created with ID {} at location {}", createdPayable.getId(), location);
        return ResponseEntity.created(location).body(createdPayable);
    }

    /**
     * Retrieves a payable by its ID.
     * Recupera uma conta a pagar pelo seu ID.
     */
    @Operation(summary = "Get Payable by ID", description = "Retrieves details of a specific account payable.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payable found", content = @Content(schema = @Schema(implementation = PayableDTO.class))),
        @ApiResponse(responseCode = "404", description = "Payable not found"),
         // Add 401 when security is implemented
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PayableDTO> getPayableById(@PathVariable Long id) {
        log.debug("Received request to get payable by ID: {}", id);
        PayableDTO payable = payableService.getPayableById(id);
        return ResponseEntity.ok(payable);
    }

    /**
     * Retrieves all payables, optionally filtered by status.
     * Recupera todas as contas a pagar, opcionalmente filtradas por status.
     */
    @Operation(summary = "Get All Payables", description = "Retrieves a list of all accounts payable, optionally filtered by status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payables retrieved successfully"),
         // Add 401 when security is implemented
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PayableDTO>> getAllPayables(
            @Parameter(description = "Filter by status (e.g., PENDING, OVERDUE)") // OpenAPI Parameter description
            @RequestParam(required = false) PayableStatus status) {
        log.debug("Received request to get all payables, status filter: {}", status);
        List<PayableDTO> payables;
        if (status != null) {
            payables = payableService.getPayablesByStatus(status);
        } else {
            payables = payableService.getAllPayables();
        }
        return ResponseEntity.ok(payables);
    }

    /**
     * Retrieves all overdue payables.
     * Recupera todas as contas a pagar atrasadas.
     */
     @Operation(summary = "Get Overdue Payables", description = "Retrieves a list of all overdue accounts payable.")
     @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overdue payables retrieved successfully"),
         // Add 401 when security is implemented
    })
    @GetMapping(value = "/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PayableDTO>> getOverduePayables() {
         log.debug("Received request to get overdue payables");
         List<PayableDTO> payables = payableService.getOverduePayables();
         return ResponseEntity.ok(payables);
     }


    /**
     * Updates an existing payable record.
     * Atualiza um registro de conta a pagar existente.
     */
    @Operation(summary = "Update Payable", description = "Updates an existing account payable record.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payable updated successfully", content = @Content(schema = @Schema(implementation = PayableDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Payable not found"),
         // Add 401/403 when security is implemented
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PayableDTO> updatePayable(@PathVariable Long id, @Valid @RequestBody PayableDTO payableDTO) {
        log.info("Received request to update payable ID: {}", id);
        PayableDTO updatedPayable = payableService.updatePayable(id, payableDTO);
        return ResponseEntity.ok(updatedPayable);
    }

     /**
     * Partially updates the status and payment details of a payable.
     * Atualiza parcialmente o status e detalhes de pagamento de uma conta a pagar.
     */
    @Operation(summary = "Update Payable Status", description = "Partially updates payable status, payment date, and amount paid.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payable status updated successfully", content = @Content(schema = @Schema(implementation = PayableDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status or amount"),
        @ApiResponse(responseCode = "404", description = "Payable not found"),
        // Add 401/403 when security is implemented
    })
    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PayableDTO> updatePayableStatus(
            @PathVariable Long id,
            @Parameter(description = "The new status (e.g., PAID, PARTIALLY_PAID)", required = true)
            @RequestParam PayableStatus status,
            @Parameter(description = "Date the payment was made (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
             @Parameter(description = "Amount paid in this transaction")
            @RequestParam(required = false) BigDecimal amountPaid) {
        log.info("Received request to update status for payable ID {} to {}", id, status);
        PayableDTO updatedPayable = payableService.updatePayableStatus(id, status, paymentDate, amountPaid);
        return ResponseEntity.ok(updatedPayable);
    }


    /**
     * Deletes a payable record.
     * Deleta um registro de conta a pagar.
     */
    @Operation(summary = "Delete Payable", description = "Deletes an account payable record by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Payable deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Payable not found"),
         // Add 401/403 when security is implemented
    })
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayable(@PathVariable Long id) {
        log.info("Received request to delete payable ID: {}", id);
        payableService.deletePayable(id);
        return ResponseEntity.noContent().build();
    }

    // --- Aggregate Endpoints for Financial Overview ---

    /**
     * Gets the total amount currently pending payment.
     * Obtém o valor total atualmente pendente de pagamento.
     */
    @Operation(summary = "Get Total Pending Amount", description = "Calculates the total amount of payables that are not yet fully paid or canceled.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Total pending amount calculated successfully")})
    @GetMapping(value = "/summary/pending-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalPendingAmount() {
        log.debug("Received request for total pending payable amount");
        return ResponseEntity.ok(payableService.getTotalPendingAmount());
    }

    /**
     * Gets the total amount currently overdue.
     * Obtém o valor total atualmente atrasado.
     */
    @Operation(summary = "Get Total Overdue Amount", description = "Calculates the total amount of payables that are past their due date and not fully paid or canceled.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Total overdue amount calculated successfully")})
    @GetMapping(value = "/summary/overdue-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getTotalOverdueAmount() {
         log.debug("Received request for total overdue payable amount");
        return ResponseEntity.ok(payableService.getTotalOverdueAmount());
    }

}
// Path: src/main/java/com/bufalari/payable/controller/PayableController.java
package com.bufalari.payable.controller;

import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.dto.PayableSummaryDTO;
import com.bufalari.payable.dto.PaymentTransactionDTO;
import com.bufalari.payable.enums.PayableStatus;
import com.bufalari.payable.service.PayableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing accounts payable and their payment transactions.
 * Controlador REST para gerenciamento de contas a pagar e suas transações de pagamento.
 */
@RestController
@RequestMapping("/api/payables")
@RequiredArgsConstructor
@Tag(name = "Accounts Payable", description = "Endpoints for managing accounts payable and payments")
@SecurityRequirement(name = "bearerAuth") // Assume security is applied
public class PayableController {

    private static final Logger log = LoggerFactory.getLogger(PayableController.class);
    private final PayableService payableService;

    // --- Payable CRUD ---

    @Operation(summary = "Create Payable", description = "Creates a new account payable record. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payable created", content = @Content(schema = @Schema(implementation = PayableDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"), @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PayableDTO> createPayable(@Valid @RequestBody PayableDTO payableDTO) {
        log.info("Request to create payable: {}", payableDTO.getDescription());
        PayableDTO createdPayable = payableService.createPayable(payableDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdPayable.getId()).toUri();
        return ResponseEntity.created(location).body(createdPayable);
    }

    @Operation(summary = "Get Payable by ID", description = "Retrieves a specific payable. Requires authenticated access.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payable found", content = @Content(schema = @Schema(implementation = PayableDTO.class))),
        @ApiResponse(responseCode = "404", description = "Payable not found"), @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<PayableDTO> getPayableById(@PathVariable Long id) {
        log.debug("Request to get payable by ID: {}", id);
        return ResponseEntity.ok(payableService.getPayableById(id));
    }

    @Operation(summary = "Get All Payables", description = "Retrieves payables, optionally filtered by status. Requires authenticated access.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payables retrieved") })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<PayableDTO>> getAllPayables(
            @Parameter(description = "Filter by status") @RequestParam(required = false) PayableStatus status) {
        log.debug("Request to get all payables, status filter: {}", status);
        List<PayableDTO> payables = (status != null) ? payableService.getPayablesByStatus(status) : payableService.getAllPayables();
        return ResponseEntity.ok(payables);
    }

     @Operation(summary = "Get Overdue Payables", description = "Retrieves overdue payables. Requires authenticated access.")
     @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Overdue payables retrieved") })
    @GetMapping(value = "/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<PayableDTO>> getOverduePayables() {
         log.debug("Request to get overdue payables");
         return ResponseEntity.ok(payableService.getOverduePayables());
     }

    @Operation(summary = "Update Payable", description = "Updates core details of an existing payable. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = { /* ... */ })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PayableDTO> updatePayable(@PathVariable Long id, @Valid @RequestBody PayableDTO payableDTO) {
        log.info("Request to update payable ID: {}", id);
        return ResponseEntity.ok(payableService.updatePayable(id, payableDTO));
    }

    @Operation(summary = "Delete Payable", description = "Deletes a payable record. Requires ADMIN role.")
    @ApiResponses(value = { /* ... */ })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayable(@PathVariable Long id) {
        log.info("Request to delete payable ID: {}", id);
        payableService.deletePayable(id);
        return ResponseEntity.noContent().build();
    }

    // --- Payment Transaction Endpoints ---

    @Operation(summary = "Register Payment Transaction", description = "Registers a payment made towards a specific payable. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = { /* ... */ })
    @PostMapping(value = "/{payableId}/payments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PaymentTransactionDTO> registerPayment(
            @PathVariable Long payableId,
            @Valid @RequestBody PaymentTransactionDTO transactionDTO) {
        log.info("Request to register payment for payable ID: {}", payableId);
        PaymentTransactionDTO registeredPayment = payableService.registerPaymentTransaction(payableId, transactionDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/payables/{payableId}/payments/{paymentId}") // Maybe point to a future GET endpoint for the transaction
                .buildAndExpand(payableId, registeredPayment.getId()).toUri();
        return ResponseEntity.created(location).body(registeredPayment);
    }

    @Operation(summary = "Get Payments for Payable", description = "Retrieves all payment transactions for a specific payable. Requires authenticated access.")
    @ApiResponses(value = { /* ... */ })
    @GetMapping(value = "/{payableId}/payments", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<PaymentTransactionDTO>> getPaymentsForPayable(@PathVariable Long payableId) {
         log.debug("Request to get payments for payable ID: {}", payableId);
         return ResponseEntity.ok(payableService.getPaymentTransactionsForPayable(payableId));
    }

    // --- Cash Flow Integration Endpoints ---

    @Operation(summary = "Get Paid Summaries by Payment Date Range", description = "Internal endpoint for cash flow service. Retrieves summaries of payables paid within a date range.")
    @GetMapping(value = "/summary-by-payment-date", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Or a specific internal role
    public ResponseEntity<List<PayableSummaryDTO>> getPaidSummariesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Internal request for paid payable summaries between {} and {}", startDate, endDate);
        return ResponseEntity.ok(payableService.getPaidPayableSummariesByPaymentDateRange(startDate, endDate));
    }

    @Operation(summary = "Get Pending Summaries by Due Date Range", description = "Internal endpoint for cash flow service. Retrieves summaries of pending payables due within a date range.")
    @GetMapping(value = "/pending-summary-by-due-date", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Or a specific internal role
    public ResponseEntity<List<PayableSummaryDTO>> getPendingSummariesByDueDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Internal request for pending payable summaries due between {} and {}", startDate, endDate);
        return ResponseEntity.ok(payableService.getPendingPayableSummariesByDueDateRange(startDate, endDate));
    }

    // --- Supplier Service Integration Endpoint ---

     @Operation(summary = "Check Active Payables for Supplier", description = "Internal endpoint for supplier service validation.")
     @GetMapping(value = "/exists-active-by-supplier/{supplierId}", produces = MediaType.APPLICATION_JSON_VALUE)
     // @PreAuthorize("isAuthenticated()") // Define specific access if needed
    public ResponseEntity<Boolean> hasActivePayables(@PathVariable Long supplierId) {
         log.debug("Internal request to check active payables for supplier ID: {}", supplierId);
         return ResponseEntity.ok(payableService.hasActivePayablesForSupplier(supplierId));
    }

    // --- Document Management Endpoints (STUBS) ---
    // (Keeping stubs as before, adjust PreAuthorize as needed)

    @PostMapping(value = "/{payableId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<String> uploadPayableDocument(@PathVariable Long payableId, @RequestParam("file") MultipartFile file) {
         log.info("Received request to upload document for payable ID: {}", payableId);
          if (file == null || file.isEmpty()) {
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
         }
         // --- STUB ---
         String documentReference = "payable-doc-stub-" + System.nanoTime();
         payableService.getPayableById(payableId); // Check existence
         log.warn("Document upload STUB called for payable {}, file '{}'. Reference: {}", payableId, file.getOriginalFilename(), documentReference);
         // --- END STUB ---
          try {
              URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                 .path("/api/payables/{payableId}/documents/{docRef}")
                 .buildAndExpand(payableId, documentReference).toUri();
             return ResponseEntity.created(location).body(documentReference);
         } catch (Exception e) {
              log.error("Failed to build location URI for payable document ID {}: {}", payableId, e.getMessage(), e);
              return ResponseEntity.ok(documentReference);
         }
    }

    @GetMapping(value = "/{payableId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<String>> getPayableDocumentReferences(@PathVariable Long payableId) {
          log.debug("Received request to get document references for payable ID: {}", payableId);
           // --- STUB ---
          PayableDTO payable = payableService.getPayableById(payableId);
          List<String> references = payable.getDocumentReferences();
          log.warn("Document references STUB returning references for payable {}", payableId);
           // --- END STUB ---
          return ResponseEntity.ok(references);
    }

    @DeleteMapping("/{payableId}/documents/{documentReference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Void> deletePayableDocumentReference(
            @PathVariable Long payableId,
            @PathVariable String documentReference) {
          log.info("Received request to delete document reference '{}' for payable ID: {}", documentReference, payableId);
           // --- STUB ---
          payableService.getPayableById(payableId); // Check existence
          log.warn("Document deletion STUB called for payable {}, reference {}", payableId, documentReference);
           // --- END STUB ---
          return ResponseEntity.noContent().build();
    }
}
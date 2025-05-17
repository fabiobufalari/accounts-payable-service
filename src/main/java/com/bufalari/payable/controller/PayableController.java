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

import java.math.BigDecimal; // Keep BigDecimal
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID; // <<<--- IMPORT UUID

/**
 * REST controller for managing accounts payable and their payment transactions.
 * Controlador REST para gerenciamento de contas a pagar e suas transações de pagamento.
 */
@RestController
@RequestMapping("/accounts-payable")
@RequiredArgsConstructor
@Tag(name = "Accounts Payable", description = "Endpoints for managing accounts payable and payments")
@SecurityRequirement(name = "bearerAuth") // Assume security is applied via SecurityConfig/JWT
public class PayableController {

    private static final Logger log = LoggerFactory.getLogger(PayableController.class);
    private final PayableService payableService;

    // --- Payable CRUD ---

    @Operation(summary = "Create Payable", description = "Creates a new account payable record. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payable created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PayableDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have required role")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PayableDTO> createPayable(@Valid @RequestBody PayableDTO payableDTO) {
        log.info("Request to create payable: {}", payableDTO.getDescription());
        PayableDTO createdPayable = payableService.createPayable(payableDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdPayable.getId()).toUri();
        log.info("Payable created with ID {}, location: {}", createdPayable.getId(), location);
        return ResponseEntity.created(location).body(createdPayable);
    }

    @Operation(summary = "Get Payable by ID", description = "Retrieves a specific payable by its UUID. Requires authenticated access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payable found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PayableDTO.class))),
            @ApiResponse(responseCode = "404", description = "Payable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<PayableDTO> getPayableById(
            @Parameter(description = "UUID of the payable to retrieve") @PathVariable UUID id) { // <<<--- UUID
        log.debug("Request to get payable by ID: {}", id);
        return ResponseEntity.ok(payableService.getPayableById(id));
    }

    @Operation(summary = "Get All Payables", description = "Retrieves payables, optionally filtered by status. Requires authenticated access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payables retrieved", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<PayableDTO>> getAllPayables(
            @Parameter(description = "Filter by payable status") @RequestParam(required = false) PayableStatus status) {
        log.debug("Request to get all payables, status filter: {}", status);
        List<PayableDTO> payables = (status != null) ? payableService.getPayablesByStatus(status) : payableService.getAllPayables();
        return ResponseEntity.ok(payables);
    }

    @Operation(summary = "Get Overdue Payables", description = "Retrieves payables that are past their due date and not fully paid. Requires authenticated access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue payables retrieved", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<PayableDTO>> getOverduePayables() {
        log.debug("Request to get overdue payables");
        return ResponseEntity.ok(payableService.getOverduePayables());
    }

    @Operation(summary = "Update Payable", description = "Updates core details of an existing payable by its UUID. Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payable updated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PayableDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Payable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PayableDTO> updatePayable(
            @Parameter(description = "UUID of the payable to update") @PathVariable UUID id, // <<<--- UUID
            @Valid @RequestBody PayableDTO payableDTO) {
        log.info("Request to update payable ID: {}", id);
        // Ensure ID in path matches ID in body if present, or ignore body ID
        if (payableDTO.getId() != null && !payableDTO.getId().equals(id)) {
            log.warn("Path ID {} does not match body ID {}. Using path ID.", id, payableDTO.getId());
            // Optionally throw bad request or just proceed with path ID
        }
        return ResponseEntity.ok(payableService.updatePayable(id, payableDTO));
    }

    @Operation(summary = "Delete Payable", description = "Deletes a payable record by its UUID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Payable deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Payable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayable(
            @Parameter(description = "UUID of the payable to delete") @PathVariable UUID id) { // <<<--- UUID
        log.info("Request to delete payable ID: {}", id);
        payableService.deletePayable(id);
        return ResponseEntity.noContent().build();
    }

    // --- Payment Transaction Endpoints ---

    @Operation(summary = "Register Payment Transaction", description = "Registers a payment made towards a specific payable (by UUID). Requires ACCOUNTANT or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment registered", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PaymentTransactionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment data"),
            @ApiResponse(responseCode = "404", description = "Payable not found"),
            @ApiResponse(responseCode = "409", description = "Operation not allowed (e.g., paying a CANCELED payable)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping(value = "/{payableId}/payments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PaymentTransactionDTO> registerPayment(
            @Parameter(description = "UUID of the payable being paid") @PathVariable UUID payableId, // <<<--- UUID
            @Valid @RequestBody PaymentTransactionDTO transactionDTO) {
        log.info("Request to register payment for payable ID: {}", payableId);
        PaymentTransactionDTO registeredPayment = payableService.registerPaymentTransaction(payableId, transactionDTO);
        // Location points to the newly created payment transaction (assuming a future GET endpoint for it)
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/payables/{payableId}/payments/{paymentId}") // Need an endpoint to GET a single payment by ID
                .buildAndExpand(payableId, registeredPayment.getId()).toUri(); // Use the UUIDs
        log.info("Payment transaction registered with ID {} for payable {}, location: {}", registeredPayment.getId(), payableId, location);
        return ResponseEntity.created(location).body(registeredPayment);
    }

    @Operation(summary = "Get Payments for Payable", description = "Retrieves all payment transactions for a specific payable (by UUID). Requires authenticated access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Payable not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping(value = "/{payableId}/payments", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<PaymentTransactionDTO>> getPaymentsForPayable(
            @Parameter(description = "UUID of the payable to get payments for") @PathVariable UUID payableId) { // <<<--- UUID
        log.debug("Request to get payments for payable ID: {}", payableId);
        return ResponseEntity.ok(payableService.getPaymentTransactionsForPayable(payableId));
    }

    // --- Cash Flow Integration Endpoints ---

    @Operation(summary = "Get Paid Summaries by Payment Date Range", description = "Internal endpoint for cash flow service. Retrieves summaries of payables based on payments made within a date range.")
    @ApiResponses(value = { /* ... add appropriate responses ... */ })
    @GetMapping(value = "/summary-by-payment-date", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Or a specific internal role/permission
    public ResponseEntity<List<PayableSummaryDTO>> getPaidSummariesByDateRange(
            @Parameter(description = "Start date (inclusive) of the payment transaction range", example = "2024-01-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive) of the payment transaction range", example = "2024-01-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Internal request for paid payable summaries (based on transaction dates) between {} and {}", startDate, endDate);
        return ResponseEntity.ok(payableService.getPaidPayableSummariesByPaymentDateRange(startDate, endDate));
    }

    @Operation(summary = "Get Pending Summaries by Due Date Range", description = "Internal endpoint for cash flow service. Retrieves summaries of pending/partially paid payables due within a date range.")
    @ApiResponses(value = { /* ... add appropriate responses ... */ })
    @GetMapping(value = "/pending-summary-by-due-date", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Or a specific internal role/permission
    public ResponseEntity<List<PayableSummaryDTO>> getPendingSummariesByDueDateRange(
            @Parameter(description = "Start date (inclusive) of the due date range", example = "2024-02-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive) of the due date range", example = "2024-02-29") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Internal request for pending payable summaries due between {} and {}", startDate, endDate);
        return ResponseEntity.ok(payableService.getPendingPayableSummariesByDueDateRange(startDate, endDate));
    }

    // --- Supplier Service Integration Endpoint ---

    @Operation(summary = "Check Active Payables for Supplier", description = "Internal endpoint for supplier service validation. Checks if a supplier has any payables that are not PAID or CANCELED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check result", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = "Invalid Supplier ID format (if applicable)")
    })
    @GetMapping(value = "/exists-active-by-supplier/{supplierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("isAuthenticated()") // Or more specific internal role/permission
    public ResponseEntity<Boolean> hasActivePayables(
            @Parameter(description = "ID of the supplier to check") @PathVariable Long supplierId) { // <<<--- supplierId remains Long
        log.debug("Internal request to check active payables for supplier ID: {}", supplierId);
        return ResponseEntity.ok(payableService.hasActivePayablesForSupplier(supplierId));
    }

    // --- Document Management Endpoints (STUBS) ---
    // (Keeping stubs as before, but updating path variables to UUID)

    @Operation(summary = "Upload Document for Payable (STUB)", description = "Attaches a document to a specific payable (by UUID). Requires ACCOUNTANT or ADMIN role. THIS IS A STUB.")
    @ApiResponses(value = { /* ... */ })
    @PostMapping(value = "/{payableId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) // Produces JSON for ref
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<String> uploadPayableDocument(
            @Parameter(description = "UUID of the payable") @PathVariable UUID payableId, // <<<--- UUID
            @Parameter(description = "The document file to upload") @RequestParam("file") MultipartFile file) {
        log.info("Received request to upload document for payable ID: {}", payableId);
        if (file == null || file.isEmpty()) {
            log.error("Upload failed for payable {}: File is empty.", payableId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }
        // --- STUB ---
        // Check if payable exists before proceeding with stub logic
        PayableDTO payable = payableService.getPayableById(payableId); // Throws 404 if not found
        String documentReference = "payable-doc-stub-" + payableId.toString() + "-" + System.nanoTime(); // Example reference
        log.warn("[STUB] Document upload called for payable {}, file '{}'. Generated Reference: {}", payableId, file.getOriginalFilename(), documentReference);
        // In a real scenario: payableService.addDocumentReference(payableId, file);
        // --- END STUB ---
        try {
            // Build location URI for the potential future GET endpoint of the document reference
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/payables/{payableId}/documents/{docRef}")
                    .buildAndExpand(payableId, documentReference).toUri();
            return ResponseEntity.created(location).body(documentReference); // Return reference in body
        } catch (Exception e) {
            log.error("Failed to build location URI for payable document ID {}: {}", payableId, e.getMessage(), e);
            // Fallback: Return 200 OK with reference if URI building fails (should not happen often)
            return ResponseEntity.ok(documentReference);
        }
    }

    @Operation(summary = "Get Document References for Payable (STUB)", description = "Retrieves a list of document references associated with a specific payable (by UUID). Requires authenticated access. THIS IS A STUB.")
    @ApiResponses(value = { /* ... */ })
    @GetMapping(value = "/{payableId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')")
    public ResponseEntity<List<String>> getPayableDocumentReferences(
            @Parameter(description = "UUID of the payable") @PathVariable UUID payableId) { // <<<--- UUID
        log.debug("Received request to get document references for payable ID: {}", payableId);
        // --- STUB ---
        // Check existence and retrieve DTO which might contain stubbed references
        PayableDTO payable = payableService.getPayableById(payableId); // Throws 404 if not found
        List<String> references = payable.getDocumentReferences(); // Get references from DTO (populated by service/converter)
        log.warn("[STUB] Document references STUB returning references for payable {}: {}", payableId, references);
        // In a real scenario: references = payableService.getDocumentReferences(payableId);
        // --- END STUB ---
        return ResponseEntity.ok(references);
    }

    @Operation(summary = "Delete Document Reference for Payable (STUB)", description = "Removes a document reference from a specific payable (by UUID). Requires ACCOUNTANT or ADMIN role. THIS IS A STUB.")
    @ApiResponses(value = { /* ... */ })
    @DeleteMapping("/{payableId}/documents/{documentReference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Void> deletePayableDocumentReference(
            @Parameter(description = "UUID of the payable") @PathVariable UUID payableId, // <<<--- UUID
            @Parameter(description = "The document reference string to delete") @PathVariable String documentReference) {
        log.info("Received request to delete document reference '{}' for payable ID: {}", documentReference, payableId);
        // --- STUB ---
        payableService.getPayableById(payableId); // Check existence first
        log.warn("[STUB] Document deletion called for payable {}, reference {}", payableId, documentReference);
        // In a real scenario: payableService.deleteDocumentReference(payableId, documentReference);
        // --- END STUB ---
        return ResponseEntity.noContent().build();
    }
}
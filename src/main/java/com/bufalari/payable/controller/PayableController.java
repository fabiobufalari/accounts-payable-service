package com.bufalari.payable.controller;


import com.bufalari.payable.dto.PayableDTO;
import com.bufalari.payable.enums.PayableStatus;
import com.bufalari.payable.service.PayableService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <<<--- IMPORTAR
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // <<<--- IMPORTAR para stubs de documento
import org.springframework.web.server.ResponseStatusException; // <<<--- IMPORTAR para stubs de documento
import java.net.URI;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // <<<--- IMPORTAR
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * REST controller for managing accounts payable. Secured endpoints.
 * Controlador REST para gerenciamento de contas a pagar. Endpoints protegidos.
 */
@RestController
@RequestMapping("/api/payables")
@RequiredArgsConstructor
@Tag(name = "Accounts Payable", description = "Endpoints for managing accounts payable")
@SecurityRequirement(name = "bearerAuth") // <<<--- ADICIONAR requirement global
public class PayableController {

    private static final Logger log = LoggerFactory.getLogger(PayableController.class);
    private final PayableService payableService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<PayableDTO> createPayable(@Valid @RequestBody PayableDTO payableDTO) {
        // ... implementation ...
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

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<PayableDTO> getPayableById(@PathVariable Long id) {
        // ... implementation ...
         log.debug("Received request to get payable by ID: {}", id);
         PayableDTO payable = payableService.getPayableById(id);
         return ResponseEntity.ok(payable);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<List<PayableDTO>> getAllPayables(
            @Parameter(description = "Filter by status (e.g., PENDING, OVERDUE)")
            @RequestParam(required = false) PayableStatus status) {
        // ... implementation ...
         log.debug("Received request to get all payables, status filter: {}", status);
         List<PayableDTO> payables;
         if (status != null) {
             payables = payableService.getPayablesByStatus(status);
         } else {
             payables = payableService.getAllPayables();
         }
         return ResponseEntity.ok(payables);
    }

    @GetMapping(value = "/overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<List<PayableDTO>> getOverduePayables() {
         // ... implementation ...
         log.debug("Received request to get overdue payables");
         List<PayableDTO> payables = payableService.getOverduePayables();
         return ResponseEntity.ok(payables);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<PayableDTO> updatePayable(@PathVariable Long id, @Valid @RequestBody PayableDTO payableDTO) {
        // ... implementation ...
         log.info("Received request to update payable ID: {}", id);
         PayableDTO updatedPayable = payableService.updatePayable(id, payableDTO);
         return ResponseEntity.ok(updatedPayable);
    }

    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<PayableDTO> updatePayableStatus(
            @PathVariable Long id,
            @Parameter(description = "The new status (e.g., PAID, PARTIALLY_PAID)", required = true)
            @RequestParam PayableStatus status,
            @Parameter(description = "Date the payment was made (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
             @Parameter(description = "Amount paid in this transaction")
            @RequestParam(required = false) BigDecimal amountPaid) {
        // ... implementation ...
         log.info("Received request to update status for payable ID {} to {}", id, status);
         PayableDTO updatedPayable = payableService.updatePayableStatus(id, status, paymentDate, amountPaid);
         return ResponseEntity.ok(updatedPayable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<Void> deletePayable(@PathVariable Long id) {
        // ... implementation ...
         log.info("Received request to delete payable ID: {}", id);
         payableService.deletePayable(id);
         return ResponseEntity.noContent().build();
    }

    // --- Aggregate Endpoints ---
    @GetMapping(value = "/summary/pending-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<BigDecimal> getTotalPendingAmount() {
        // ... implementation ...
         log.debug("Received request for total pending payable amount");
         return ResponseEntity.ok(payableService.getTotalPendingAmount());
    }

    @GetMapping(value = "/summary/overdue-amount", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // <<<--- DESCOMENTAR/AJUSTAR ROLE
    public ResponseEntity<BigDecimal> getTotalOverdueAmount() {
         // ... implementation ...
         log.debug("Received request for total overdue payable amount");
         return ResponseEntity.ok(payableService.getTotalOverdueAmount());
    }

    // --- DOCUMENT MANAGEMENT ENDPOINTS (STUBS - Add security as needed) ---

    @PostMapping(value = "/{payableId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')") // Example: Roles that can upload docs
    public ResponseEntity<String> uploadPayableDocument(@PathVariable Long payableId, @RequestParam("file") MultipartFile file) {
        log.info("Received request to upload document for payable ID: {}", payableId);
        if (file == null || file.isEmpty()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }
        // --- STUB ---
        // TODO: Call service layer to interact with document-storage-service
        // String documentReference = payableService.addDocumentReference(payableId, file);
        String documentReference = "payable-doc-stub-" + System.currentTimeMillis();
        log.warn("Document upload STUB called for payable {}, file '{}'. Reference: {}", payableId, file.getOriginalFilename(), documentReference);
        payableService.getPayableById(payableId); // Simulate check if payable exists
        // --- END STUB ---
        try {
             URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/payables/{payableId}/documents/{docRef}")
                .buildAndExpand(payableId, documentReference).toUri();
            return ResponseEntity.created(location).body(documentReference);
        } catch (Exception e) {
            log.error("Failed to build location URI for uploaded payable document for ID {}: {}", payableId, e.getMessage(), e);
            return ResponseEntity.ok(documentReference); // Return 200 OK even if URI fails for stub
        }
    }


    @GetMapping(value = "/{payableId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'FINANCIAL_VIEWER')") // Example: Roles that can view docs
    public ResponseEntity<List<String>> getPayableDocumentReferences(@PathVariable Long payableId) {
        log.debug("Received request to get document references for payable ID: {}", payableId);
        // --- STUB ---
        // TODO: Call service layer to get references
        // List<String> references = payableService.getDocumentReferences(payableId);
        PayableDTO payable = payableService.getPayableById(payableId); // Ensures payable exists
        List<String> references = payable.getDocumentReferences(); // Get from DTO for stub
        log.warn("Document references STUB returning references for payable {}", payableId);
        // --- END STUB ---
        return ResponseEntity.ok(references);
    }


    @DeleteMapping("/{payableId}/documents/{documentReference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')") // Example: Roles that can delete doc refs
    public ResponseEntity<Void> deletePayableDocumentReference(
            @PathVariable Long payableId,
            @PathVariable String documentReference) {
        log.info("Received request to delete document reference '{}' for payable ID: {}", documentReference, payableId);
        // --- STUB ---
        // TODO: Call service layer to remove reference and potentially delete from storage
        // payableService.deleteDocumentReference(payableId, documentReference);
        payableService.getPayableById(payableId); // Simulate check if payable exists
        log.warn("Document deletion STUB called for payable {}, reference {}", payableId, documentReference);
        // --- END STUB ---
        return ResponseEntity.noContent().build();
    }
}
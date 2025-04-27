// Path: src/main/java/com/bufalari/payable/exception/OperationNotAllowedException.java
package com.bufalari.payable.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an operation cannot be performed due to business rules
 * (e.g., deleting a resource with active dependencies). Maps to HTTP 409 Conflict.
 * Exceção lançada quando uma operação não pode ser realizada devido a regras de negócio
 * (ex: deletar um recurso com dependências ativa). Mapeia para HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict is often appropriate here
public class OperationNotAllowedException extends RuntimeException {
    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.modulith.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando dados de entrada são inválidos.
 * Retorna HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    public ValidationException(String field, String value, String reason) {
        super(String.format("Invalid %s '%s': %s", field, value, reason));
    }
}

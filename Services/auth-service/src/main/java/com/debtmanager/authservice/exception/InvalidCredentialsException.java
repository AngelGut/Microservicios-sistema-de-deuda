package com.debtmanager.authservice.exception;

/**
 * Excepción lanzada cuando las credenciales del usuario no son válidas.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

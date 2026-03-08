package com.debtmanager.authservice.exception;

/**
 * Excepción lanzada cuando un token JWT es inválido,
 * está mal formado o no puede verificarse.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}

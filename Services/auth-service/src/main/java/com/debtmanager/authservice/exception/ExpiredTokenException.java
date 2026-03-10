package com.debtmanager.authservice.exception;

/**
 * Excepción lanzada cuando un token JWT ya expiró.
 */
public class ExpiredTokenException extends RuntimeException {

    public ExpiredTokenException(String message) {
        super(message);
    }
}

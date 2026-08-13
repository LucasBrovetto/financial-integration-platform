package io.github.lucasbrovetto.financialintegration.acquirer.application.exception;

public final class AcquirerProcessingException extends RuntimeException {

    public AcquirerProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

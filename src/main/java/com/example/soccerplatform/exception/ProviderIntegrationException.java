package com.example.soccerplatform.exception;

public class ProviderIntegrationException extends RuntimeException {

    public ProviderIntegrationException(String message) {
        super(message);
    }

    public ProviderIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}

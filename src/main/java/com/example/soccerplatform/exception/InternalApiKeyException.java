package com.example.soccerplatform.exception;

public class InternalApiKeyException extends RuntimeException {

    public InternalApiKeyException() {
        super("Unauthorized");
    }
}

package com.ikea.oms.orchestratorservice.exception;

public class SagaOrderNotFoundException extends RuntimeException {
    public SagaOrderNotFoundException(String message) {
        super(message);
    }
}

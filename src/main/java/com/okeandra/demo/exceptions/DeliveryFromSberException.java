package com.okeandra.demo.exceptions;

public class DeliveryFromSberException extends RuntimeException {
    private String message;

    public DeliveryFromSberException(String superMessage, String localMessage) {
        super(superMessage);
        this.message = localMessage;
    }
}

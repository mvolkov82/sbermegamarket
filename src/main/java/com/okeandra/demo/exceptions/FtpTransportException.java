package com.okeandra.demo.exceptions;

public class FtpTransportException extends RuntimeException {
    private String message;

    public FtpTransportException(String superMessage, String localMessage) {
        super(superMessage);
        this.message = localMessage;
    }
}

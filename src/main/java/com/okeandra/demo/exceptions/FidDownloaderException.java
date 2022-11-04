package com.okeandra.demo.exceptions;

public class FidDownloaderException extends Exception {
    private String message;

    public FidDownloaderException(String superMessage, String localMessage) {
        super(superMessage);
        this.message = localMessage;
    }
}

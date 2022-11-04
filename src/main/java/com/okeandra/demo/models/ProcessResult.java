package com.okeandra.demo.models;

public class ProcessResult {
    private boolean isSuccess;
    private String logMessage;

    public ProcessResult(boolean isSuccess, String logMessage) {
        this.isSuccess = isSuccess;
        this.logMessage = logMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
}

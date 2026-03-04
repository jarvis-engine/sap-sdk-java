package com.vengine.kk.sap.common.exception;

public class SapClientException extends RuntimeException {

    public SapClientException(String message) {
        super(message);
    }

    public SapClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

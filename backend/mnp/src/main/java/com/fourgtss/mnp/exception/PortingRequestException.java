package com.fourgtss.mnp.exception;

public class PortingRequestException extends RuntimeException {

    private final PortingRequestErrorCode errorCode;

    public PortingRequestException(PortingRequestErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public PortingRequestErrorCode getErrorCode() {
        return errorCode;
    }
}

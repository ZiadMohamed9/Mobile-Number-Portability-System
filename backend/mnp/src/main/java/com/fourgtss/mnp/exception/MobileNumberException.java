package com.fourgtss.mnp.exception;

public class MobileNumberException extends RuntimeException {

    private final MobileNumberErrorCode errorCode;

    public MobileNumberException(MobileNumberErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public MobileNumberErrorCode getErrorCode() {
        return errorCode;
    }
}

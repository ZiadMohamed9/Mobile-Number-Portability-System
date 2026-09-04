package com.fourgtss.mnp.dto;

public record MobileNumberResponse(
        String phoneNumber,
        String nationalIdLast4,
        String serviceStatus,
        String currentOperatorSince,
        String originOperatorCode,
        String currentOperatorCode
) {
}

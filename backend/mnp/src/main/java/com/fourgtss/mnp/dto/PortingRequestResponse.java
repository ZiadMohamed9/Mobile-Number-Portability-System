package com.fourgtss.mnp.dto;

import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import com.fourgtss.mnp.models.enums.RejectionReason;

import java.time.Instant;

public record PortingRequestResponse(
        long id,
        String phoneNumber,
        String donorOperatorCode,
        String recipientOperatorCode,
        PortingRequestStatus status,
        Instant requestedAt,
        Instant expiresAt,
        Instant resolvedAt,
        RejectionReason rejectionReason
) {
}

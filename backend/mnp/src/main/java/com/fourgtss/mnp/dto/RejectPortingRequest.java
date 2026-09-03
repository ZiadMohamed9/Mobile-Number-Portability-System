package com.fourgtss.mnp.dto;

import com.fourgtss.mnp.models.enums.RejectionReason;
import jakarta.validation.constraints.NotNull;

public record RejectPortingRequest(
        @NotNull
        RejectionReason rejectionReason
) {
}

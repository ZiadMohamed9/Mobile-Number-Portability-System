package com.fourgtss.mnp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePortingRequest(
        @NotBlank
        @Pattern(regexp = "^01\\d{9}$")
        String phoneNumber,

        @NotBlank
        @Pattern(regexp = "^\\d{14}$")
        String nationalId
) {
}

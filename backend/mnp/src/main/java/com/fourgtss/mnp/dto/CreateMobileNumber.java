package com.fourgtss.mnp.dto;

import com.fourgtss.mnp.models.enums.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateMobileNumber(
        @NotBlank
        @Pattern(regexp = "^01\\d{9}$")
        String phoneNumber,

        @NotBlank
        @Pattern(regexp = "^\\d{14}$")
        String nationalId,

        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotNull
        ServiceStatus serviceStatus,

        @NotNull
        LocalDate currentOperatorSince,

        @NotBlank
        String operatorCode
) {
}

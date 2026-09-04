package com.fourgtss.mnp.service;

import com.fourgtss.mnp.exception.PortingRequestErrorCode;
import com.fourgtss.mnp.exception.PortingRequestException;
import com.fourgtss.mnp.models.MobileNumber;
import com.fourgtss.mnp.models.Operator;
import com.fourgtss.mnp.models.enums.ServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PortingEligibilityService {

    private static final int MINIMUM_TENURE_MONTHS = 4;

    private final NationalIdVerifier nationalIdVerifier;
    private final Clock clock;

    public void validate(MobileNumber mobileNumber, Operator recipientOperator, String nationalId) {
        verifyNationalId(mobileNumber, nationalId);
        verifyServiceStatus(mobileNumber);
        verifyRecipientOperator(mobileNumber, recipientOperator);
        verifyMinimumTenure(mobileNumber, clock.instant());
    }

    private void verifyNationalId(MobileNumber mobileNumber, String nationalId) {
        if (!nationalIdVerifier.matches(nationalId, mobileNumber.getSubscriber().getNationalIdHmac())) {
            throw new PortingRequestException(PortingRequestErrorCode.NATIONAL_ID_MISMATCH);
        }
    }

    private void verifyServiceStatus(MobileNumber mobileNumber) {
        if (mobileNumber.getServiceStatus() != ServiceStatus.ACTIVE) {
            throw new PortingRequestException(PortingRequestErrorCode.NUMBER_NOT_ACTIVE);
        }
    }

    private void verifyRecipientOperator(MobileNumber mobileNumber, Operator recipientOperator) {
        if (mobileNumber.getCurrentOperator().equals(recipientOperator)) {
            throw new PortingRequestException(PortingRequestErrorCode.SAME_OPERATOR);
        }
    }

    private void verifyMinimumTenure(MobileNumber mobileNumber, Instant now) {
        Instant eligibleAt = mobileNumber.getCurrentOperatorSince()
                .atZone(ZoneOffset.UTC)
                .plusMonths(MINIMUM_TENURE_MONTHS)
                .toInstant();
        if (now.isBefore(eligibleAt)) {
            throw new PortingRequestException(PortingRequestErrorCode.MINIMUM_TENURE_NOT_MET);
        }
    }
}

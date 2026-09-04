package com.fourgtss.mnp.service;

import com.fourgtss.mnp.dto.PortingRequestResponse;
import com.fourgtss.mnp.dto.RejectPortingRequest;
import com.fourgtss.mnp.exception.PortingRequestErrorCode;
import com.fourgtss.mnp.exception.PortingRequestException;
import com.fourgtss.mnp.mapper.PortingRequestResponseMapper;
import com.fourgtss.mnp.models.MobileNumber;
import com.fourgtss.mnp.models.Operator;
import com.fourgtss.mnp.models.PortingRequest;
import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import com.fourgtss.mnp.models.enums.ServiceStatus;
import com.fourgtss.mnp.repository.MobileNumberRepository;
import com.fourgtss.mnp.repository.PortingRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PortingRequestDecisionService {

    private final MobileNumberRepository mobileNumberRepository;
    private final PortingRequestRepository portingRequestRepository;
    private final OperatorResolver operatorResolver;
    private final PortingRequestResponseMapper portingRequestResponseMapper;
    private final Clock clock;

    @Transactional
    public PortingRequestResponse accept(Long requestId, String donorOperatorCode) {
        LockedDecision lockedDecision = lockDecision(requestId, donorOperatorCode);
        Instant resolvedAt = clock.instant();

        if (expireIfDue(lockedDecision.portingRequest(), resolvedAt)) {
            return portingRequestResponseMapper.from(lockedDecision.portingRequest());
        }

        verifyNumberCanPort(lockedDecision);
        lockedDecision.portingRequest().accept(resolvedAt);
        lockedDecision.mobileNumber().portTo(lockedDecision.portingRequest().getRecipientOperator(), resolvedAt);

        return portingRequestResponseMapper.from(lockedDecision.portingRequest());
    }

    @Transactional
    public PortingRequestResponse reject(
            Long requestId,
            String donorOperatorCode,
            RejectPortingRequest rejection) {
        LockedDecision lockedDecision = lockDecision(requestId, donorOperatorCode);
        Instant resolvedAt = clock.instant();
        if (expireIfDue(lockedDecision.portingRequest(), resolvedAt)) {
            return portingRequestResponseMapper.from(lockedDecision.portingRequest());
        }
        lockedDecision.portingRequest().reject(rejection.rejectionReason(), resolvedAt);
        return portingRequestResponseMapper.from(lockedDecision.portingRequest());
    }

    private LockedDecision lockDecision(Long requestId, String donorOperatorCode) {
        Operator donorOperator = operatorResolver.requireByCode(donorOperatorCode);

        String phoneNumber = portingRequestRepository.findPhoneNumberById(requestId)
                .orElseThrow(() -> new PortingRequestException(PortingRequestErrorCode.PORTING_REQUEST_NOT_FOUND));

        MobileNumber mobileNumber = mobileNumberRepository.findForPortingRequest(phoneNumber)
                .orElseThrow(() -> new PortingRequestException(PortingRequestErrorCode.MOBILE_NUMBER_NOT_FOUND));

        PortingRequest portingRequest = portingRequestRepository.findForDecision(requestId)
                .orElseThrow(() -> new PortingRequestException(PortingRequestErrorCode.PORTING_REQUEST_NOT_FOUND));

        verifyDonor(portingRequest, donorOperator);
        verifyPending(portingRequest);

        return new LockedDecision(mobileNumber, portingRequest);
    }

    private boolean expireIfDue(PortingRequest portingRequest, Instant resolvedAt) {
        if (!portingRequest.isExpiredAt(resolvedAt)) {
            return false;
        }

        portingRequest.cancelTimeout(resolvedAt);
        return true;
    }

    private void verifyNumberCanPort(LockedDecision lockedDecision) {
        if (lockedDecision.mobileNumber().getServiceStatus() != ServiceStatus.ACTIVE) {
            throw new PortingRequestException(PortingRequestErrorCode.NUMBER_NOT_ACTIVE);
        }

        if (!lockedDecision.mobileNumber().getCurrentOperator()
                .equals(lockedDecision.portingRequest().getDonorOperator())) {
            throw new PortingRequestException(PortingRequestErrorCode.DONOR_NO_LONGER_CURRENT);
        }
    }

    private void verifyDonor(PortingRequest portingRequest, Operator donorOperator) {
        if (!portingRequest.getDonorOperator().equals(donorOperator)) {
            throw new PortingRequestException(PortingRequestErrorCode.NOT_DONOR);
        }
    }

    private void verifyPending(PortingRequest portingRequest) {
        if (portingRequest.getStatus() != PortingRequestStatus.PENDING) {
            throw new PortingRequestException(PortingRequestErrorCode.REQUEST_NOT_PENDING);
        }
    }

    private record LockedDecision(MobileNumber mobileNumber, PortingRequest portingRequest) {
    }
}

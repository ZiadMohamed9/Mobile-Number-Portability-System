package com.fourgtss.mnp.service;

import com.fourgtss.mnp.dto.CreatePortingRequest;
import com.fourgtss.mnp.dto.PortingRequestResponse;
import com.fourgtss.mnp.exception.PortingRequestErrorCode;
import com.fourgtss.mnp.exception.PortingRequestException;
import com.fourgtss.mnp.mapper.PortingRequestResponseMapper;
import com.fourgtss.mnp.models.MobileNumber;
import com.fourgtss.mnp.models.Operator;
import com.fourgtss.mnp.models.PortingRequest;
import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import com.fourgtss.mnp.repository.MobileNumberRepository;
import com.fourgtss.mnp.repository.PortingRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortingRequestSubmissionService {

    private final MobileNumberRepository mobileNumberRepository;
    private final PortingRequestRepository portingRequestRepository;
    private final OperatorResolver operatorResolver;
    private final PortingEligibilityService portingEligibilityService;
    private final PortingRequestResponseMapper portingRequestResponseMapper;

    @Transactional
    public PortingRequestResponse create(CreatePortingRequest request, String recipientOperatorCode) {
        Operator recipientOperator = operatorResolver.requireByCode(recipientOperatorCode);
        MobileNumber mobileNumber = lockMobileNumber(request.phoneNumber());
        portingRequestRepository.cancelExpiredPending(mobileNumber.getPhoneNumber());
        rejectDuplicatePendingRequest(mobileNumber);
        portingEligibilityService.validate(mobileNumber, recipientOperator, request.nationalId());

        PortingRequest pendingRequest = PortingRequest.pending(
                mobileNumber,
                recipientOperator,
                mobileNumber.getCurrentOperator());

        return portingRequestResponseMapper.from(portingRequestRepository.saveAndFlush(pendingRequest));
    }

    private MobileNumber lockMobileNumber(String phoneNumber) {
        return mobileNumberRepository.findForPortingRequest(phoneNumber)
                .orElseThrow(() -> new PortingRequestException(PortingRequestErrorCode.MOBILE_NUMBER_NOT_FOUND));
    }

    private void rejectDuplicatePendingRequest(MobileNumber mobileNumber) {
        if (portingRequestRepository.existsByMobileNumberPhoneNumberAndStatus(
                mobileNumber.getPhoneNumber(),
                PortingRequestStatus.PENDING)) {
            throw new PortingRequestException(PortingRequestErrorCode.PENDING_REQUEST_EXISTS);
        }
    }
}

package com.fourgtss.mnp.service;

import com.fourgtss.mnp.dto.PortingRequestResponse;
import com.fourgtss.mnp.dto.PortingRequestView;
import com.fourgtss.mnp.exception.PortingRequestErrorCode;
import com.fourgtss.mnp.exception.PortingRequestException;
import com.fourgtss.mnp.mapper.PortingRequestResponseMapper;
import com.fourgtss.mnp.models.Operator;
import com.fourgtss.mnp.models.PortingRequest;
import com.fourgtss.mnp.models.enums.PortingRequestStatus;
import com.fourgtss.mnp.repository.PortingRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortingRequestQueryService {

    private final PortingRequestRepository portingRequestRepository;
    private final OperatorResolver operatorResolver;
    private final PortingRequestResponseMapper portingRequestResponseMapper;

    @Transactional(readOnly = true)
    public PortingRequestResponse getById(Long requestId, String actorOperatorCode) {
        Operator actorOperator = operatorResolver.requireByCode(actorOperatorCode);

        PortingRequest portingRequest = portingRequestRepository.findDetailedById(requestId)
                .orElseThrow(() -> new PortingRequestException(PortingRequestErrorCode.PORTING_REQUEST_NOT_FOUND));

        verifyVisibility(portingRequest, actorOperator);

        return portingRequestResponseMapper.from(portingRequest);
    }

    @Transactional(readOnly = true)
    public Page<PortingRequestResponse> listRequests(
            String actorOperatorCode,
            PortingRequestView view,
            Pageable pageable) {
        Operator actorOperator = operatorResolver.requireByCode(actorOperatorCode);

        Page<PortingRequest> requests = switch (view) {
            case ACCEPTED -> portingRequestRepository.findAccepted(pageable);
            case RECIPIENT -> portingRequestRepository.findByRecipientOperator(actorOperator.getId(), pageable);
            case DONOR -> portingRequestRepository.findByDonorOperator(actorOperator.getId(), pageable);
            case null -> portingRequestRepository.findVisibleToOperator(actorOperator.getId(), pageable);
        };

        return requests.map(portingRequestResponseMapper::from);
    }

    private void verifyVisibility(PortingRequest portingRequest, Operator actorOperator) {
        if (portingRequest.getStatus() == PortingRequestStatus.ACCEPTED
                || portingRequest.getDonorOperator().equals(actorOperator)
                || portingRequest.getRecipientOperator().equals(actorOperator)) {
            return;
        }
        throw new PortingRequestException(PortingRequestErrorCode.PORTING_REQUEST_NOT_FOUND);
    }
}

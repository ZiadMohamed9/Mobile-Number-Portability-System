package com.fourgtss.mnp.mapper;

import com.fourgtss.mnp.dto.PortingRequestResponse;
import com.fourgtss.mnp.models.PortingRequest;
import org.springframework.stereotype.Component;

@Component
public class PortingRequestResponseMapper {

    public PortingRequestResponse from(PortingRequest portingRequest) {
        return new PortingRequestResponse(
                portingRequest.getId(),
                portingRequest.getMobileNumber().getPhoneNumber(),
                portingRequest.getDonorOperator().getCode(),
                portingRequest.getRecipientOperator().getCode(),
                portingRequest.getStatus(),
                portingRequest.getRequestedAt(),
                portingRequest.getExpiresAt(),
                portingRequest.getResolvedAt(),
                portingRequest.getRejectionReason()
        );
    }
}

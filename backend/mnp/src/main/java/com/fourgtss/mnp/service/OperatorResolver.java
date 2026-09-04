package com.fourgtss.mnp.service;

import com.fourgtss.mnp.exception.PortingRequestErrorCode;
import com.fourgtss.mnp.exception.PortingRequestException;
import com.fourgtss.mnp.models.Operator;
import com.fourgtss.mnp.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperatorResolver {

    private final OperatorRepository operatorRepository;

    public Operator requireByCode(String operatorCode) {
        if (operatorCode == null) {
            throw new PortingRequestException(PortingRequestErrorCode.UNKNOWN_ORGANIZATION);
        }
        return operatorRepository.findByCode(operatorCode.trim().toUpperCase())
                .orElseThrow(() -> new PortingRequestException(PortingRequestErrorCode.UNKNOWN_ORGANIZATION));
    }
}

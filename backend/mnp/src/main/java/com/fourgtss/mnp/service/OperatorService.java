package com.fourgtss.mnp.service;

import com.fourgtss.mnp.dto.OperatorDto;
import com.fourgtss.mnp.repository.OperatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OperatorService {
    private final OperatorRepository operatorRepository;

    public List<OperatorDto> getOperators() {
        return operatorRepository.findAll().stream()
                .map(op -> new OperatorDto(op.getId(), op.getCode(), op.getDisplayName(), op.getNumberPrefix()))
                .toList();
    }
}

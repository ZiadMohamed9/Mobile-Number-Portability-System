package com.fourgtss.mnp.service;

import com.fourgtss.mnp.repository.PortingRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortingRequestExpiryService {

    private final PortingRequestRepository portingRequestRepository;

    @Transactional
    public void expireDueRequests() {
        portingRequestRepository.cancelAllExpiredPending();
    }
}

package com.fourgtss.mnp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortingRequestExpiryScheduler {

    private final PortingRequestExpiryService portingRequestExpiryService;

    @Scheduled(fixedDelay = 30_000)
    public void expireDueRequests() {
        portingRequestExpiryService.expireDueRequests();
    }
}

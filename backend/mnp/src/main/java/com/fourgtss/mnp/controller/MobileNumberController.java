package com.fourgtss.mnp.controller;

import com.fourgtss.mnp.dto.CreateMobileNumber;
import com.fourgtss.mnp.dto.MobileNumberResponse;
import com.fourgtss.mnp.service.MobileNumberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile-numbers")
@RequiredArgsConstructor
public class MobileNumberController {

    private final MobileNumberService creationService;

    @PostMapping
    public ResponseEntity<MobileNumberResponse> create(@Valid @RequestBody CreateMobileNumber request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(creationService.create(request));
    }
}

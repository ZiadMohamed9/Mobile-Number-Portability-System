package com.fourgtss.mnp.controller;

import com.fourgtss.mnp.dto.CreatePortingRequest;
import com.fourgtss.mnp.dto.PortingRequestResponse;
import com.fourgtss.mnp.dto.RejectPortingRequest;
import com.fourgtss.mnp.service.PortingRequestDecisionService;
import com.fourgtss.mnp.service.PortingRequestQueryService;
import com.fourgtss.mnp.service.PortingRequestSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/porting-requests")
@RequiredArgsConstructor
public class PortingRequestController {

    private final PortingRequestSubmissionService submissionService;
    private final PortingRequestQueryService queryService;
    private final PortingRequestDecisionService decisionService;

    @PostMapping
    public ResponseEntity<PortingRequestResponse> create(
            @RequestHeader("Organization") String operatorCode,
            @Valid @RequestBody CreatePortingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.create(request, operatorCode));
    }

    @GetMapping
    public ResponseEntity<Page<PortingRequestResponse>> list(
            @RequestHeader("Organization") String operatorCode,
            Pageable pageable) {
        return ResponseEntity.ok(queryService.listVisibleRequests(operatorCode, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortingRequestResponse> getById(
            @RequestHeader("Organization") String operatorCode,
            @PathVariable Long id) {
        return ResponseEntity.ok(queryService.getById(id, operatorCode));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<PortingRequestResponse> accept(
            @RequestHeader("Organization") String operatorCode,
            @PathVariable Long id) {
        return ResponseEntity.ok(decisionService.accept(id, operatorCode));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PortingRequestResponse> reject(
            @RequestHeader("Organization") String operatorCode,
            @PathVariable Long id,
            @Valid @RequestBody RejectPortingRequest request) {
        return ResponseEntity.ok(decisionService.reject(id, operatorCode, request));
    }
}

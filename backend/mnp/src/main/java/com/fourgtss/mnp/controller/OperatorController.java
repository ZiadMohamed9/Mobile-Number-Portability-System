package com.fourgtss.mnp.controller;

import com.fourgtss.mnp.dto.OperatorDto;
import com.fourgtss.mnp.service.OperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operators")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping
    public ResponseEntity<List<OperatorDto>> list() {
        return ResponseEntity.ok(operatorService.getOperators());
    }
}

package com.bolivar.seguros.polizas.controller;

import com.bolivar.seguros.polizas.dto.RiskResponse;
import com.bolivar.seguros.polizas.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riesgos")
@RequiredArgsConstructor
@Tag(name = "Risk Management", description = "Endpoints for managing individual risks")
public class RiskController {

    private final PolicyService policyService;

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancel a specific risk")
    public ResponseEntity<RiskResponse> cancelRisk(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.cancelRisk(id));
    }
}

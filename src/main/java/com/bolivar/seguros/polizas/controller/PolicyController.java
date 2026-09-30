package com.bolivar.seguros.polizas.controller;

import com.bolivar.seguros.polizas.dto.CreatePolicyRequest;
import com.bolivar.seguros.polizas.dto.CreateRiskRequest;
import com.bolivar.seguros.polizas.dto.PolicyResponse;
import com.bolivar.seguros.polizas.dto.RiskResponse;
import com.bolivar.seguros.polizas.model.PolicyStatus;
import com.bolivar.seguros.polizas.model.PolicyType;
import com.bolivar.seguros.polizas.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/polizas")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "Endpoints for managing policies and their lifecycle")
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    @Operation(summary = "List all policies", description = "Filters by type (INDIVIDUAL, COLECTIVA) and status (ACTIVA, RENOVADA, CANCELADA) optionally")
    public ResponseEntity<List<PolicyResponse>> getPolicies(
            @RequestParam(required = false) PolicyType tipo,
            @RequestParam(required = false) PolicyStatus estado) {
        return ResponseEntity.ok(policyService.listPolicies(tipo, estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID", description = "Retrieves policy details and its associated risks")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new policy", description = "Creates a policy (INDIVIDUAL max 1 risk, COLECTIVA N risks)")
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(request));
    }

    @GetMapping("/{id}/riesgos")
    @Operation(summary = "List risks of a policy")
    public ResponseEntity<List<RiskResponse>> getRisks(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.listRisksByPolicy(id));
    }

    @PostMapping("/{id}/renovar")
    @Operation(summary = "Renew a policy", description = "Increments premium by IPC (5%), extends validity period and sets state to RENOVADA")
    public ResponseEntity<PolicyResponse> renewPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.renewPolicy(id));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancel a policy", description = "Cancels the policy and cascades the cancellation to all its risks")
    public ResponseEntity<PolicyResponse> cancelPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.cancelPolicy(id));
    }

    @PostMapping("/{id}/riesgos")
    @Operation(summary = "Add a risk to a policy", description = "Only allowed for COLECTIVA policies")
    public ResponseEntity<RiskResponse> addRisk(@PathVariable Long id, @Valid @RequestBody CreateRiskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.addRisk(id, request));
    }
}

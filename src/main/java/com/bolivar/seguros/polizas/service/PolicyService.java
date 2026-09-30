package com.bolivar.seguros.polizas.service;

import com.bolivar.seguros.polizas.dto.PolicyResponse;
import com.bolivar.seguros.polizas.dto.RiskResponse;
import com.bolivar.seguros.polizas.event.CoreNotificationEvent;
import com.bolivar.seguros.polizas.exception.BusinessException;
import com.bolivar.seguros.polizas.model.PolicyStatus;
import com.bolivar.seguros.polizas.model.Policy;
import com.bolivar.seguros.polizas.model.Risk;
import com.bolivar.seguros.polizas.model.PolicyType;
import com.bolivar.seguros.polizas.repository.PolicyRepository;
import com.bolivar.seguros.polizas.repository.RiskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service orchestrating the core business logic for Policies and Risks.
 * Implements strict rules for renewal, cancellation, and relationship constraints.
 */
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final RiskRepository riskRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional(readOnly = true)
    public List<PolicyResponse> listPolicies(PolicyType type, PolicyStatus status) {
        List<Policy> policies;
        if (type != null && status != null) {
            policies = policyRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            policies = policyRepository.findByType(type);
        } else if (status != null) {
            policies = policyRepository.findByStatus(status);
        } else {
            policies = policyRepository.findAll();
        }
        return policies.stream().map(this::mapToPolicyResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RiskResponse> listRisksByPolicy(Long id) {
        Policy policy = findPolicyOrThrow(id);
        return policy.getRisks().stream().map(this::mapToRiskResponse).collect(Collectors.toList());
    }

    /**
     * Renews an existing policy.
     * Applies a simulated IPC increment (5%) to the current rent and premium.
     * 
     * @param id The policy ID
     * @return PolicyResponse with updated values
     * @throws BusinessException if the policy is already CANCELADA
     */
    @Transactional
    public PolicyResponse renewPolicy(Long id) {
        Policy policy = findPolicyOrThrow(id);

        if (policy.getStatus() == PolicyStatus.CANCELADA) {
            throw new BusinessException("Cannot renew a canceled policy.");
        }

        // Increment rent and premium by IPC (e.g., 5% = 1.05)
        BigDecimal ipc = new BigDecimal("1.05");
        policy.setRentAmount(policy.getRentAmount().multiply(ipc));
        policy.setPremiumAmount(policy.getPremiumAmount().multiply(ipc));
        
        policy.setStatus(PolicyStatus.RENOVADA);
        
        notifyCoreAsync(policy.getId(), "ACTUALIZACION");

        return mapToPolicyResponse(policyRepository.save(policy));
    }

    /**
     * Cancels a policy and performs a cascading cancellation to all its associated risks.
     */
    @Transactional
    public PolicyResponse cancelPolicy(Long id) {
        Policy policy = findPolicyOrThrow(id);
        policy.setStatus(PolicyStatus.CANCELADA);
        
        for (Risk risk : policy.getRisks()) {
            risk.setStatus("CANCELADO");
        }

        notifyCoreAsync(policy.getId(), "ACTUALIZACION");

        return mapToPolicyResponse(policyRepository.save(policy));
    }

    /**
     * Adds a risk to a policy. Validates that INDIVIDUAL policies cannot exceed 1 risk.
     */
    @Transactional
    public RiskResponse addRisk(Long policyId, Risk risk) {
        Policy policy = findPolicyOrThrow(policyId);

        if (policy.getType() != PolicyType.COLECTIVA) {
            throw new BusinessException("Risks can only be added to COLECTIVA policies. INDIVIDUAL policies are limited to 1 risk.");
        }

        risk.setStatus("ACTIVO");
        policy.addRisk(risk);
        
        notifyCoreAsync(policy.getId(), "ACTUALIZACION");

        return mapToRiskResponse(riskRepository.save(risk));
    }

    @Transactional
    public RiskResponse cancelRisk(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new BusinessException("Risk not found."));
        risk.setStatus("CANCELADO");
        
        notifyCoreAsync(risk.getPolicy().getId(), "ACTUALIZACION");
        
        return mapToRiskResponse(riskRepository.save(risk));
    }

    private Policy findPolicyOrThrow(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Policy not found."));
    }

    /**
     * Publishes an event to notify the legacy CORE asynchronously without blocking the thread.
     */
    private void notifyCoreAsync(Long policyId, String eventType) {
        eventPublisher.publishEvent(new CoreNotificationEvent(policyId, eventType));
    }

    // --- Mappers ---
    
    private PolicyResponse mapToPolicyResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .type(policy.getType())
                .status(policy.getStatus())
                .validityMonths(policy.getValidityMonths())
                .rentAmount(policy.getRentAmount())
                .premiumAmount(policy.getPremiumAmount())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .risks(policy.getRisks() != null ? 
                        policy.getRisks().stream().map(this::mapToRiskResponse).collect(Collectors.toList()) : null)
                .build();
    }

    private RiskResponse mapToRiskResponse(Risk risk) {
        return RiskResponse.builder()
                .id(risk.getId())
                .description(risk.getDescription())
                .status(risk.getStatus())
                .createdAt(risk.getCreatedAt())
                .updatedAt(risk.getUpdatedAt())
                .build();
    }
}

package com.bolivar.seguros.polizas.service;

import com.bolivar.seguros.polizas.dto.CreatePolicyRequest;
import com.bolivar.seguros.polizas.dto.CreateRiskRequest;
import com.bolivar.seguros.polizas.dto.PolicyResponse;
import com.bolivar.seguros.polizas.dto.RiskResponse;
import com.bolivar.seguros.polizas.event.CoreNotificationEvent;
import com.bolivar.seguros.polizas.exception.BusinessException;
import com.bolivar.seguros.polizas.exception.ResourceNotFoundException;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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
    public PolicyResponse getPolicyById(Long id) {
        Policy policy = findPolicyOrThrow(id);
        return mapToPolicyResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<RiskResponse> listRisksByPolicy(Long id) {
        Policy policy = findPolicyOrThrow(id);
        return policy.getRisks().stream().map(this::mapToRiskResponse).collect(Collectors.toList());
    }

    @Transactional
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        if (request.getType() == null) {
            throw new BusinessException("El tipo de póliza es obligatorio (INDIVIDUAL o COLECTIVA).");
        }
        if (request.getValidityMonths() == null || request.getValidityMonths() <= 0) {
            throw new BusinessException("La vigencia en meses debe ser mayor a 0.");
        }
        if (request.getRentAmount() == null || request.getRentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El valor del canon mensual debe ser mayor a 0.");
        }

        // Regla esencial de negocio: Una póliza individual solo puede tener 1 riesgo
        if (request.getType() == PolicyType.INDIVIDUAL && request.getRisks() != null && request.getRisks().size() > 1) {
            throw new BusinessException("Una póliza individual solo puede tener 1 riesgo.");
        }

        BigDecimal rent = request.getRentAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal premium = request.getPremiumAmount();
        if (premium == null) {
            premium = rent.multiply(BigDecimal.valueOf(request.getValidityMonths()));
        }
        premium = premium.setScale(2, RoundingMode.HALF_UP);

        LocalDate start = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : start.plusMonths(request.getValidityMonths()).minusDays(1);

        Policy policy = Policy.builder()
                .type(request.getType())
                .status(PolicyStatus.ACTIVA)
                .validityMonths(request.getValidityMonths())
                .rentAmount(rent)
                .premiumAmount(premium)
                .startDate(start)
                .endDate(end)
                .risks(new ArrayList<>())
                .build();

        if (request.getRisks() != null) {
            for (CreateRiskRequest rReq : request.getRisks()) {
                Risk r = Risk.builder()
                        .description(rReq.getDescription())
                        .status("ACTIVO")
                        .policy(policy)
                        .build();
                policy.addRisk(r);
            }
        }

        Policy savedPolicy = policyRepository.save(policy);
        notifyCoreAsync(savedPolicy.getId(), "ACTUALIZACION");

        return mapToPolicyResponse(savedPolicy);
    }

    /**
     * Renueva una póliza existente.
     * Incrementa canon y prima según el IPC (5%) aplicando redondeo a 2 decimales
     * y extiende la vigencia por el mismo período inicial.
     * 
     * @param id Identificador de la póliza
     * @return PolicyResponse con valores y vigencia actualizados
     * @throws BusinessException si la póliza está CANCELADA
     */
    @Transactional
    public PolicyResponse renewPolicy(Long id) {
        Policy policy = findPolicyOrThrow(id);

        if (policy.getStatus() == PolicyStatus.CANCELADA) {
            throw new BusinessException("Cannot renew a canceled policy.");
        }

        // Increment rent and premium by IPC (e.g., 5% = 1.05) con escala monetaria a 2 decimales
        BigDecimal ipc = new BigDecimal("1.05");
        policy.setRentAmount(policy.getRentAmount().multiply(ipc).setScale(2, RoundingMode.HALF_UP));
        policy.setPremiumAmount(policy.getPremiumAmount().multiply(ipc).setScale(2, RoundingMode.HALF_UP));
        
        // Extender el período de vigencia por el mismo plazo original
        if (policy.getEndDate() != null && policy.getValidityMonths() != null) {
            LocalDate newStartDate = policy.getEndDate().plusDays(1);
            LocalDate newEndDate = newStartDate.plusMonths(policy.getValidityMonths()).minusDays(1);
            policy.setStartDate(newStartDate);
            policy.setEndDate(newEndDate);
        }

        policy.setStatus(PolicyStatus.RENOVADA);
        
        Policy savedPolicy = policyRepository.save(policy);
        notifyCoreAsync(savedPolicy.getId(), "ACTUALIZACION");

        return mapToPolicyResponse(savedPolicy);
    }

    /**
     * Cancela una póliza y propaga la cancelación a todos sus riesgos asociados.
     */
    @Transactional
    public PolicyResponse cancelPolicy(Long id) {
        Policy policy = findPolicyOrThrow(id);
        
        if (policy.getStatus() == PolicyStatus.CANCELADA) {
            throw new BusinessException("La póliza ya se encuentra cancelada.");
        }

        policy.setStatus(PolicyStatus.CANCELADA);
        
        if (policy.getRisks() != null) {
            for (Risk risk : policy.getRisks()) {
                risk.setStatus("CANCELADO");
            }
        }

        Policy savedPolicy = policyRepository.save(policy);
        notifyCoreAsync(savedPolicy.getId(), "ACTUALIZACION");

        return mapToPolicyResponse(savedPolicy);
    }

    /**
     * Agrega un riesgo a una póliza. Valida que solo aplique para pólizas COLECTIVAS
     * y que la póliza no esté cancelada.
     */
    @Transactional
    public RiskResponse addRisk(Long policyId, CreateRiskRequest request) {
        if (request == null || request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BusinessException("La descripción del riesgo es obligatoria.");
        }
        return addRiskInternal(policyId, request.getDescription());
    }

    @Transactional
    public RiskResponse addRisk(Long policyId, Risk risk) {
        if (risk == null || risk.getDescription() == null || risk.getDescription().isBlank()) {
            throw new BusinessException("La descripción del riesgo es obligatoria.");
        }
        return addRiskInternal(policyId, risk.getDescription());
    }

    private RiskResponse addRiskInternal(Long policyId, String description) {
        Policy policy = findPolicyOrThrow(policyId);

        if (policy.getStatus() == PolicyStatus.CANCELADA) {
            throw new BusinessException("No se pueden agregar riesgos a una póliza cancelada.");
        }

        if (policy.getType() != PolicyType.COLECTIVA) {
            throw new BusinessException("Solo se pueden agregar riesgos a pólizas de tipo COLECTIVA. Las pólizas individuales solo pueden tener 1 riesgo.");
        }

        Risk risk = Risk.builder()
                .description(description)
                .status("ACTIVO")
                .policy(policy)
                .build();

        policy.addRisk(risk);
        Risk savedRisk = riskRepository.save(risk);
        
        notifyCoreAsync(policy.getId(), "ACTUALIZACION");

        return mapToRiskResponse(savedRisk);
    }

    @Transactional
    public RiskResponse cancelRisk(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new ResourceNotFoundException("Riesgo no encontrado con ID: " + riskId));
        
        if ("CANCELADO".equalsIgnoreCase(risk.getStatus())) {
            throw new BusinessException("El riesgo ya se encuentra cancelado.");
        }

        risk.setStatus("CANCELADO");
        Risk savedRisk = riskRepository.save(risk);
        
        notifyCoreAsync(risk.getPolicy().getId(), "ACTUALIZACION");
        
        return mapToRiskResponse(savedRisk);
    }

    private Policy findPolicyOrThrow(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza no encontrada con ID: " + id));
    }

    /**
     * Publica evento asíncrono para notificar al CORE legado sin bloquear el hilo HTTP.
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

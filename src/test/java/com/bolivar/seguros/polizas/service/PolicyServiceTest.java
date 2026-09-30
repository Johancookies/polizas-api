package com.bolivar.seguros.polizas.service;

import com.bolivar.seguros.polizas.dto.CreatePolicyRequest;
import com.bolivar.seguros.polizas.dto.CreateRiskRequest;
import com.bolivar.seguros.polizas.dto.PolicyResponse;
import com.bolivar.seguros.polizas.dto.RiskResponse;
import com.bolivar.seguros.polizas.event.CoreNotificationEvent;
import com.bolivar.seguros.polizas.exception.BusinessException;
import com.bolivar.seguros.polizas.exception.ResourceNotFoundException;
import com.bolivar.seguros.polizas.model.Policy;
import com.bolivar.seguros.polizas.model.PolicyStatus;
import com.bolivar.seguros.polizas.model.PolicyType;
import com.bolivar.seguros.polizas.model.Risk;
import com.bolivar.seguros.polizas.repository.PolicyRepository;
import com.bolivar.seguros.polizas.repository.RiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private RiskRepository riskRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PolicyService policyService;

    private Policy individualPolicy;
    private Policy colectivaPolicy;

    @BeforeEach
    void setUp() {
        individualPolicy = Policy.builder()
                .id(1L)
                .type(PolicyType.INDIVIDUAL)
                .status(PolicyStatus.ACTIVA)
                .validityMonths(12)
                .rentAmount(new BigDecimal("1000.00"))
                .premiumAmount(new BigDecimal("12000.00"))
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .risks(new ArrayList<>())
                .build();

        colectivaPolicy = Policy.builder()
                .id(2L)
                .type(PolicyType.COLECTIVA)
                .status(PolicyStatus.ACTIVA)
                .validityMonths(24)
                .rentAmount(new BigDecimal("5000.00"))
                .premiumAmount(new BigDecimal("120000.00"))
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2028, 5, 31))
                .risks(new ArrayList<>())
                .build();
    }

    @Test
    void renewPolicy_Success_IncrementsAmountsAndExtendsValidity() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(individualPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        PolicyResponse response = policyService.renewPolicy(1L);

        // Assert
        assertEquals(PolicyStatus.RENOVADA, response.getStatus());
        // 1000.00 * 1.05 = 1050.00
        assertEquals(0, new BigDecimal("1050.00").compareTo(response.getRentAmount()));
        // 12000.00 * 1.05 = 12600.00
        assertEquals(0, new BigDecimal("12600.00").compareTo(response.getPremiumAmount()));
        
        // Verificación de extensión de vigencia
        assertEquals(LocalDate.of(2027, 1, 1), response.getStartDate());
        assertEquals(LocalDate.of(2027, 12, 31), response.getEndDate());

        verify(eventPublisher, times(1)).publishEvent(any(CoreNotificationEvent.class));
    }

    @Test
    void renewPolicy_ThrowsException_IfPolicyIsCanceled() {
        // Arrange
        individualPolicy.setStatus(PolicyStatus.CANCELADA);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(individualPolicy));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            policyService.renewPolicy(1L);
        });
        
        assertEquals("Cannot renew a canceled policy.", exception.getMessage());
        verify(policyRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void addRisk_Success_WhenPolicyIsColectiva() {
        // Arrange
        when(policyRepository.findById(2L)).thenReturn(Optional.of(colectivaPolicy));
        when(riskRepository.save(any(Risk.class))).thenAnswer(i -> i.getArguments()[0]);

        CreateRiskRequest request = CreateRiskRequest.builder()
                .description("Riesgo Terremoto")
                .build();

        // Act
        RiskResponse response = policyService.addRisk(2L, request);

        // Assert
        assertNotNull(response);
        assertEquals("ACTIVO", response.getStatus());
        assertEquals("Riesgo Terremoto", response.getDescription());
        assertEquals(1, colectivaPolicy.getRisks().size());
        
        verify(eventPublisher, times(1)).publishEvent(any(CoreNotificationEvent.class));
    }

    @Test
    void addRisk_ThrowsException_WhenPolicyIsIndividual() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(individualPolicy));
        CreateRiskRequest request = CreateRiskRequest.builder().description("Riesgo no permitido").build();

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            policyService.addRisk(1L, request);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden agregar riesgos a pólizas de tipo COLECTIVA"));
        verify(riskRepository, never()).save(any());
    }

    @Test
    void cancelPolicy_Success_CascadesToRisks() {
        // Arrange
        Risk activeRisk = new Risk();
        activeRisk.setStatus("ACTIVO");
        individualPolicy.addRisk(activeRisk);
        
        when(policyRepository.findById(1L)).thenReturn(Optional.of(individualPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        PolicyResponse response = policyService.cancelPolicy(1L);

        // Assert
        assertEquals(PolicyStatus.CANCELADA, response.getStatus());
        assertEquals("CANCELADO", individualPolicy.getRisks().get(0).getStatus());
        verify(eventPublisher, times(1)).publishEvent(any(CoreNotificationEvent.class));
    }

    @Test
    void createPolicy_IndividualWithMoreThanOneRisk_ThrowsException() {
        // Arrange
        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .type(PolicyType.INDIVIDUAL)
                .validityMonths(12)
                .rentAmount(new BigDecimal("1000.00"))
                .risks(List.of(
                        new CreateRiskRequest("Riesgo 1"),
                        new CreateRiskRequest("Riesgo 2")
                ))
                .build();

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            policyService.createPolicy(request);
        });
        assertEquals("Una póliza individual solo puede tener 1 riesgo.", ex.getMessage());
        verify(policyRepository, never()).save(any());
    }

    @Test
    void getPolicyById_NotFound_ThrowsResourceNotFoundException() {
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            policyService.getPolicyById(999L);
        });
    }
}

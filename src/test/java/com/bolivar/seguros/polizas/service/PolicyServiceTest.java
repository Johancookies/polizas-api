package com.bolivar.seguros.polizas.service;

import com.bolivar.seguros.polizas.dto.PolicyResponse;
import com.bolivar.seguros.polizas.dto.RiskResponse;
import com.bolivar.seguros.polizas.event.CoreNotificationEvent;
import com.bolivar.seguros.polizas.exception.BusinessException;
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
import java.util.ArrayList;
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
                .rentAmount(new BigDecimal("1000.00"))
                .premiumAmount(new BigDecimal("12000.00"))
                .risks(new ArrayList<>())
                .build();

        colectivaPolicy = Policy.builder()
                .id(2L)
                .type(PolicyType.COLECTIVA)
                .status(PolicyStatus.ACTIVA)
                .rentAmount(new BigDecimal("5000.00"))
                .premiumAmount(new BigDecimal("120000.00"))
                .risks(new ArrayList<>())
                .build();
    }

    @Test
    void renewPolicy_Success_IncrementsAmountsAndSetsStatus() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(individualPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        PolicyResponse response = policyService.renewPolicy(1L);

        // Assert
        assertEquals(PolicyStatus.RENOVADA, response.getStatus());
        // 1000 * 1.05 = 1050
        assertEquals(0, new BigDecimal("1050.00").compareTo(response.getRentAmount()));
        // 12000 * 1.05 = 12600
        assertEquals(0, new BigDecimal("12600.00").compareTo(response.getPremiumAmount()));
        
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

        Risk newRisk = new Risk();
        newRisk.setDescription("Earthquake");

        // Act
        RiskResponse response = policyService.addRisk(2L, newRisk);

        // Assert
        assertNotNull(response);
        assertEquals("ACTIVO", response.getStatus());
        assertEquals("Earthquake", response.getDescription());
        assertEquals(1, colectivaPolicy.getRisks().size());
        
        verify(eventPublisher, times(1)).publishEvent(any(CoreNotificationEvent.class));
    }

    @Test
    void addRisk_ThrowsException_WhenPolicyIsIndividual() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(individualPolicy));
        Risk newRisk = new Risk();

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            policyService.addRisk(1L, newRisk);
        });

        assertTrue(exception.getMessage().contains("INDIVIDUAL policies are limited to 1 risk"));
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
}

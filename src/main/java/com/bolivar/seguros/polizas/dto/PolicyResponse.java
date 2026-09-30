package com.bolivar.seguros.polizas.dto;

import com.bolivar.seguros.polizas.model.PolicyStatus;
import com.bolivar.seguros.polizas.model.PolicyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PolicyResponse {
    private Long id;
    private PolicyType type;
    private PolicyStatus status;
    private Integer validityMonths;
    private BigDecimal rentAmount;
    private BigDecimal premiumAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RiskResponse> risks;
}

package com.bolivar.seguros.polizas.dto;

import com.bolivar.seguros.polizas.model.PolicyStatus;
import com.bolivar.seguros.polizas.model.PolicyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonProperty("tipo")
    public PolicyType getTipo() {
        return type;
    }

    @JsonProperty("estado")
    public PolicyStatus getEstado() {
        return status;
    }

    @JsonProperty("vigenciaMeses")
    public Integer getVigenciaMeses() {
        return validityMonths;
    }

    @JsonProperty("valorCanon")
    public BigDecimal getValorCanon() {
        return rentAmount;
    }

    @JsonProperty("valorPrima")
    public BigDecimal getValorPrima() {
        return premiumAmount;
    }

    @JsonProperty("fechaInicio")
    public LocalDate getFechaInicio() {
        return startDate;
    }

    @JsonProperty("fechaFin")
    public LocalDate getFechaFin() {
        return endDate;
    }

    @JsonProperty("riesgos")
    public List<RiskResponse> getRiesgos() {
        return risks;
    }
}

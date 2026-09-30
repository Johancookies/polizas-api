package com.bolivar.seguros.polizas.dto;

import com.bolivar.seguros.polizas.model.PolicyType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePolicyRequest {

    @NotNull(message = "El tipo de póliza es obligatorio (INDIVIDUAL o COLECTIVA)")
    @JsonProperty("tipo")
    @JsonAlias({"type"})
    private PolicyType type;

    @NotNull(message = "La vigencia en meses es obligatoria")
    @Min(value = 1, message = "La vigencia mínima es de 1 mes")
    @JsonProperty("vigenciaMeses")
    @JsonAlias({"validityMonths", "vigencia_meses"})
    private Integer validityMonths;

    @NotNull(message = "El valor del canon mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor del canon debe ser mayor a 0")
    @JsonProperty("valorCanon")
    @JsonAlias({"rentAmount", "valor_canon"})
    private BigDecimal rentAmount;

    @JsonProperty("valorPrima")
    @JsonAlias({"premiumAmount", "valor_prima"})
    private BigDecimal premiumAmount;

    @JsonProperty("fechaInicio")
    @JsonAlias({"startDate", "fecha_inicio"})
    private LocalDate startDate;

    @JsonProperty("fechaFin")
    @JsonAlias({"endDate", "fecha_fin"})
    private LocalDate endDate;

    @JsonProperty("riesgos")
    @JsonAlias({"risks"})
    private List<CreateRiskRequest> risks;
}

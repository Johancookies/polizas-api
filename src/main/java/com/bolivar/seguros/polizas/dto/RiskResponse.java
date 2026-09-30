package com.bolivar.seguros.polizas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskResponse {
    private Long id;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonProperty("descripcion")
    public String getDescripcion() {
        return description;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return status;
    }
}

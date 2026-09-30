package com.bolivar.seguros.polizas.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRiskRequest {

    @NotBlank(message = "La descripción del riesgo no puede estar vacía")
    @JsonProperty("descripcion")
    @JsonAlias({"description"})
    private String description;
}

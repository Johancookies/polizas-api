package com.bolivar.seguros.polizas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreEventRequest {
    @JsonProperty("evento")
    private String event;
    
    @JsonProperty("polizaId")
    private Long policyId;
}

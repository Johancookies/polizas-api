package com.bolivar.seguros.polizas.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PolicyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Seguridad: Petición sin Header de API Key debe retornar 401 Unauthorized")
    void requestWithoutApiKey_Returns401() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("No autorizado")));
    }

    @Test
    @DisplayName("Seguridad: Petición con Header oficial 'x.api-key: 123456' debe retornar 200 OK")
    void requestWithOfficialXApiKey_Returns200() throws Exception {
        mockMvc.perform(get("/polizas")
                        .header("x.api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("Seguridad: Petición con Header alternativo 'api-key: 123456' debe retornar 200 OK")
    void requestWithApiKey_Returns200() throws Exception {
        mockMvc.perform(get("/polizas")
                        .header("api-key", "123456"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger/OpenAPI: /v3/api-docs es público sin requerir API Key")
    void openApiDocs_IsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()));
    }

    @Test
    @DisplayName("Filtro: GET /polizas con parámetros case-insensitive en minúsculas funciona correctamente")
    void getPolicies_WithLowerCaseFilters_ReturnsFilteredList() throws Exception {
        mockMvc.perform(get("/polizas")
                        .param("tipo", "individual")
                        .param("estado", "activa")
                        .header("x.api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo", everyItem(is("INDIVIDUAL"))))
                .andExpect(jsonPath("$[*].estado", everyItem(is("ACTIVA"))));
    }

    @Test
    @DisplayName("Negocio: POST /polizas/{id}/renovar incrementa canon y prima en 5% y pasa a RENOVADA")
    void renewPolicy_IncrementsRentAndPremiumByIPC() throws Exception {
        mockMvc.perform(post("/polizas/1/renovar")
                        .header("x.api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.estado", is("RENOVADA")))
                .andExpect(jsonPath("$.valorCanon", is(1050.00)))
                .andExpect(jsonPath("$.valorPrima", is(12600.00)))
                .andExpect(jsonPath("$.fechaInicio", is("2027-01-01")))
                .andExpect(jsonPath("$.fechaFin", is("2027-12-31")));
    }

    @Test
    @DisplayName("Regla de Negocio: No se puede renovar una póliza cancelada (retorna 400 Bad Request)")
    void renewCanceledPolicy_Returns400BadRequest() throws Exception {
        mockMvc.perform(post("/polizas/3/renovar")
                        .header("x.api-key", "123456"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Cannot renew a canceled policy.")));
    }

    @Test
    @DisplayName("Regla de Negocio: Cancelar póliza cancela en cascada todos sus riesgos")
    void cancelPolicy_CancelsAllAssociatedRisks() throws Exception {
        mockMvc.perform(post("/polizas/2/cancelar")
                        .header("x.api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADA")))
                .andExpect(jsonPath("$.riesgos[*].estado", everyItem(is("CANCELADO"))));
    }

    @Test
    @DisplayName("Regla de Negocio: Agregar riesgo en español a póliza COLECTIVA es exitoso")
    void addRisk_ToColectivaPolicy_Success() throws Exception {
        String payload = "{\"descripcion\": \"Riesgo por Terremoto e Inundación\"}";

        mockMvc.perform(post("/polizas/2/riesgos")
                        .header("x.api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion", is("Riesgo por Terremoto e Inundación")))
                .andExpect(jsonPath("$.estado", is("ACTIVO")));
    }

    @Test
    @DisplayName("Regla de Negocio: Agregar riesgo a póliza INDIVIDUAL es rechazado (400 Bad Request)")
    void addRisk_ToIndividualPolicy_Returns400() throws Exception {
        String payload = "{\"descripcion\": \"Riesgo no permitido\"}";

        mockMvc.perform(post("/polizas/1/riesgos")
                        .header("x.api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Solo se pueden agregar riesgos a pólizas de tipo COLECTIVA")));
    }

    @Test
    @DisplayName("Cancelar riesgo individual: POST /riesgos/{id}/cancelar actualiza el estado a CANCELADO")
    void cancelSpecificRisk_Success() throws Exception {
        mockMvc.perform(post("/riesgos/1/cancelar")
                        .header("x.api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
    }

    @Test
    @DisplayName("Mock CORE obligatorio: POST /core-mock/evento registra evento exitosamente")
    void coreMockEvent_ReturnsSuccess() throws Exception {
        String payload = "{\"evento\": \"ACTUALIZACION\", \"polizaId\": 555}";

        mockMvc.perform(post("/core-mock/evento")
                        .header("x.api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Event registered in CORE MOCK successfully.")));
    }

    @Test
    @DisplayName("Recurso no encontrado: Consultar póliza inexistente retorna 404 Not Found")
    void getNonExistentPolicy_Returns404() throws Exception {
        mockMvc.perform(get("/polizas/9999")
                        .header("x.api-key", "123456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Recurso no encontrado")));
    }
}

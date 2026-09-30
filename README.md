# Policy Management API - Seguros Bolívar

![Java](https://img.shields.io/badge/Java-21_LTS-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen.svg)
![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)

Una API RESTful empresarial y robusta construida con **Spring Boot 3.3.4** y **Java 21** para la gestión del ciclo de vida de pólizas de arrendamiento (Individuales y Colectivas) y sus riesgos asociados, con integración asíncrona hacia el sistema CORE transaccional legado.

---

## 🚀 Características y Reglas de Negocio Implementadas

- **Gestión del Ciclo de Vida:** Creación (`POST /polizas`), consulta individual (`GET /polizas/{id}`), filtrado (`GET /polizas`), renovación (`POST /polizas/{id}/renovar`) y cancelación (`POST /polizas/{id}/cancelar`).
- **Gestión de Riesgos:** Agregar riesgos a pólizas colectivas (`POST /polizas/{id}/riesgos`) y cancelación individual de riesgos (`POST /riesgos/{id}/cancelar`).
- **Regla Esencial 1:** Una póliza individual solo puede tener 1 riesgo asociado. Agregar riesgos vía endpoint solo está permitido para pólizas de tipo `COLECTIVA`.
- **Regla Esencial 2:** No se puede renovar una póliza cancelada (retorna `400 Bad Request`).
- **Regla Esencial 3:** La cancelación de una póliza cancela en cascada todos sus riesgos asociados.
- **Ajuste Financiero por IPC:** En la renovación de póliza, el canon y la prima se incrementan en +5% IPC con redondeo financiero a dos decimales (`RoundingMode.HALF_UP`) y se extiende la vigencia por el período inicial.
- **Integración Asíncrona con el CORE Legado (`@Async`):** Cada operación que modifique estados emite un evento desacoplado que consume el endpoint mock de edición (`POST /core-mock/evento`) sin bloquear el hilo HTTP ni comprometer la disponibilidad.
- **Seguridad:** Control de acceso mediante interceptor HTTP que valida el header obligatorio `x.api-key: 123456` (con soporte alternativo para `api-key: 123456`).
- **Manejo Centralizado de Excepciones:** `@ControllerAdvice` con respuestas JSON claras ante violaciones de negocio (400), recursos no encontrados (404) y errores de validación.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Persistencia:** Spring Data JPA, H2 In-Memory Database
- **Documentación Interactiva:** OpenAPI 3 / Swagger UI (`springdoc-openapi`)
- **Herramientas:** Maven Wrapper (`mvnw`), Lombok

---

## ⚙️ Cómo Ejecutar el Proyecto en Local

### Prerrequisitos
- JDK 21+ instalado en el equipo.
- Configurar la variable `JAVA_HOME` apuntando al JDK 21:
  ```bash
  # En macOS (con Homebrew):
  export JAVA_HOME=/opt/homebrew/opt/openjdk@21
  export PATH="$JAVA_HOME/bin:$PATH"
  ```

### Inicio de la Aplicación
```bash
./mvnw spring-boot:run
```
La aplicación iniciará en `http://localhost:8080`. La base de datos H2 se inicializa y precarga automáticamente con datos semilla desde `data.sql`.

---

## 📚 Documentación Interactiva (Swagger UI)

Una vez en ejecución, puedes interactuar y probar visualmente todos los endpoints:

👉 **[Swagger UI: http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**
👉 **[OpenAPI JSON: http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

*Nota: En Swagger UI haz clic en el botón **"Authorize"** e introduce `123456` para ejecutar peticiones directamente desde el explorador.*

---

## 🔒 Autenticación y Seguridad

Todos los endpoints transaccionales exigen el header obligatorio:
```http
x.api-key: 123456
```
*(También se admite `api-key: 123456`). Las rutas de documentación Swagger UI y consola H2 son públicas para facilitar la revisión.*

---

## 🧪 Pruebas Automatizadas

El proyecto cuenta con una suite completa de **pruebas unitarias** (`PolicyServiceTest`) y **pruebas de integración web** (`PolicyControllerIntegrationTest`) con `MockMvc` cubriendo seguridad, cálculo de IPC, extensión de fechas y todas las reglas de negocio.

Para ejecutar todas las pruebas:
```bash
./mvnw clean test
```

---

## 📋 Ejemplos Rápidos con cURL

```bash
# 1. Listar todas las pólizas
curl -i -H "x.api-key: 123456" http://localhost:8080/polizas

# 2. Filtrar pólizas por tipo y estado (soporta mayúsculas y minúsculas)
curl -i -H "x.api-key: 123456" "http://localhost:8080/polizas?tipo=individual&estado=activa"

# 3. Renovar una póliza activa (+5% IPC y extiende vigencia)
curl -i -X POST -H "x.api-key: 123456" http://localhost:8080/polizas/1/renovar

# 4. Intentar renovar una póliza cancelada (retorna 400 Bad Request)
curl -i -X POST -H "x.api-key: 123456" http://localhost:8080/polizas/3/renovar

# 5. Agregar riesgo a póliza colectiva
curl -i -X POST -H "x.api-key: 123456" -H "Content-Type: application/json" \
  -d '{"descripcion": "Riesgo de Terremoto"}' \
  http://localhost:8080/polizas/2/riesgos

# 6. Intentar agregar riesgo a póliza individual (retorna 400 Bad Request)
curl -i -X POST -H "x.api-key: 123456" -H "Content-Type: application/json" \
  -d '{"descripcion": "Riesgo no permitido"}' \
  http://localhost:8080/polizas/1/riesgos

# 7. Cancelar póliza y sus riesgos en cascada
curl -i -X POST -H "x.api-key: 123456" http://localhost:8080/polizas/2/cancelar

# 8. Cancelar un riesgo específico
curl -i -X POST -H "x.api-key: 123456" http://localhost:8080/riesgos/1/cancelar

# 9. Endpoint Mock del CORE
curl -i -X POST -H "x.api-key: 123456" -H "Content-Type: application/json" \
  -d '{"evento": "ACTUALIZACION", "polizaId": 555}' \
  http://localhost:8080/core-mock/evento
```

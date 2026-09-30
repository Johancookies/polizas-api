package com.bolivar.seguros.polizas.event;

import com.bolivar.seguros.polizas.dto.CoreEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CoreNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(CoreNotificationListener.class);

    @Value("${server.port:8080}")
    private int serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @EventListener
    public void handleCoreNotification(CoreNotificationEvent event) {
        logger.info("⚡ [ASYNC] Procesando notificación al CORE para Póliza ID: {}, Tipo: {}", 
                event.getPolicyId(), event.getEventType());
        
        try {
            CoreEventRequest request = CoreEventRequest.builder()
                    .event(event.getEventType())
                    .policyId(event.getPolicyId())
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x.api-key", "123456");
            headers.set("api-key", "123456");

            HttpEntity<CoreEventRequest> entity = new HttpEntity<>(request, headers);

            String url = "http://localhost:" + serverPort + "/core-mock/evento";
            restTemplate.postForEntity(url, entity, String.class);
            
            logger.info("✅ [ASYNC] Notificación enviada con éxito al Mock del CORE: {}", request);
            
        } catch (Exception e) {
            logger.warn("⚠️ [ASYNC] No se pudo conectar vía HTTP con el Mock del CORE ({}) - continuando sin bloquear operación", e.getMessage());
        }
    }
}

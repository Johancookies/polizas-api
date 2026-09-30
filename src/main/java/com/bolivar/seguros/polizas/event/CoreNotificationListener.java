package com.bolivar.seguros.polizas.event;

import com.bolivar.seguros.polizas.dto.CoreEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CoreNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(CoreNotificationListener.class);

    @Async
    @EventListener
    public void handleCoreNotification(CoreNotificationEvent event) {
        logger.info("⚡ [ASYNC] Processing Core Notification for Policy: {}, Type: {}", 
                event.getPolicyId(), event.getEventType());
        
        try {
            CoreEventRequest request = CoreEventRequest.builder()
                    .event(event.getEventType())
                    .policyId(event.getPolicyId())
                    .build();
            
            // Real HTTP call to the local mock endpoint
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            restTemplate.postForEntity("http://localhost:8080/core-mock/evento", request, String.class);
            
            logger.info("✅ [ASYNC] Successfully invoked CORE MOCK via HTTP with payload: {}", request);
            
        } catch (Exception e) {
            logger.error("❌ [ASYNC] Failed to notify core via HTTP", e);
        }
    }
}

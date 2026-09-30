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
            // Simulate network delay to CORE
            Thread.sleep(1000); 
            
            CoreEventRequest request = CoreEventRequest.builder()
                    .event(event.getEventType())
                    .policyId(event.getPolicyId())
                    .build();
            
            logger.info("✅ [ASYNC] Successfully simulated REST call to CORE with payload: {}", request);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("❌ [ASYNC] Failed to notify core", e);
        }
    }
}

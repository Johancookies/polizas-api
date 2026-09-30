package com.bolivar.seguros.polizas.controller;

import com.bolivar.seguros.polizas.dto.CoreEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private static final Logger logger = LoggerFactory.getLogger(CoreMockController.class);

    @PostMapping("/evento")
    public ResponseEntity<String> receiveEvent(@RequestBody CoreEventRequest request) {
        logger.info("MOCK CORE - Attempting to send operation to legacy CORE. Event: {}, PolicyId: {}", 
                request.getEvent(), request.getPolicyId());
        return ResponseEntity.ok("Event registered in CORE MOCK successfully.");
    }
}

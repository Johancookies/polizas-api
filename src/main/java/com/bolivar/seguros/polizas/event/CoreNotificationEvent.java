package com.bolivar.seguros.polizas.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoreNotificationEvent {
    private final Long policyId;
    private final String eventType;
}

package com.messageforge.integration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryResult {
    private final boolean success;
    private final String externalMessageId;
    private final String errorMessage;
    
    public static DeliveryResult success(String externalMessageId) {
        return new DeliveryResult(true, externalMessageId, null);
    }
    
    public static DeliveryResult failure(String errorMessage) {
        return new DeliveryResult(false, null, errorMessage);
    }
}

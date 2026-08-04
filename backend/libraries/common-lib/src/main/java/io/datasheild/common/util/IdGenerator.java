package io.datasheild.common.util;

import java.util.UUID;

/**
 * ID generation utilities
 */
public class IdGenerator {
    
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
    
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
    
    public static UUID generateEntityId() {
        return UUID.randomUUID();
    }
}

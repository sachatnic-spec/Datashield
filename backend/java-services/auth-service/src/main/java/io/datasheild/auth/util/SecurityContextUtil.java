package io.datasheild.auth.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import io.jsonwebtoken.Claims;
import io.datasheild.auth.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility for extracting security context information from Spring Security
 * Supports JWT token claims extraction
 */
@Slf4j
public class SecurityContextUtil {
    
    private static final ThreadLocal<Map<String, Object>> sessionAttributes = ThreadLocal.withInitial(HashMap::new);

    /**
     * Get current authenticated user ID from JWT token
     */
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        // Extract userId from JWT claims (principal)
        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
            try {
                return UUID.fromString((String) principal);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID in security context: {}", principal);
                return null;
            }
        }
        
        return null;
    }

    /**
     * Get current tenant ID from JWT token
     */
    public static UUID getCurrentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        // Look for tenantId in JWT claims
        // This would require JwtAuthenticationToken or similar to store it
        // For now, return null - should be enhanced with actual JWT parsing
        return null;
    }

    /**
     * Validate that user has access to tenant
     * Throws UnauthorizedException if not authorized
     */
    public static void validateTenantAccess(UUID userId, UUID tenantId) {
        // In production: Check user's tenant membership in database
        // For now, just log the access attempt
        log.debug("Validating tenant access for user {} to tenant {}", userId, tenantId);
        
        // TODO: Implement actual tenant access validation
        // Example:
        // UserTenant userTenant = userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
        //     .orElseThrow(() -> new UnauthorizedException("User does not have access to this tenant"));
    }

    /**
     * Get user's roles from JWT token
     */
    public static java.util.Set<String> getCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Collections.emptySet();
        }
        
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Store session attribute in thread-local (for temporary data like TOTP secrets)
     */
    public static void setSessionAttribute(String key, Object value) {
        sessionAttributes.get().put(key, value);
    }

    /**
     * Get session attribute from thread-local
     */
    public static Object getSessionAttribute(String key) {
        return sessionAttributes.get().get(key);
    }

    /**
     * Remove session attribute from thread-local
     */
    public static void removeSessionAttribute(String key) {
        sessionAttributes.get().remove(key);
    }

    /**
     * Clear all session attributes
     */
    public static void clearSessionAttributes() {
        sessionAttributes.get().clear();
    }

    /**
     * Check if user has specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains("ROLE_" + role);
    }
}

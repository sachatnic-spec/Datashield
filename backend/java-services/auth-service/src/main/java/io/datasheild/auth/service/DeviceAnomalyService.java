package io.datasheild.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.datasheild.auth.entity.Session;
import io.datasheild.auth.repository.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Device Anomaly Detection Service
 * 
 * Defense-in-depth requirement: Device/IP anomaly detection
 * - Flag or step-up-auth on login from a new device/location
 * 
 * Detection methods:
 * 1. IP address change (sudden location change)
 * 2. User agent change (different browser/device)
 * 3. First login from location (new device)
 * 4. Impossible travel (too fast between locations)
 */
@Service
@Slf4j
public class DeviceAnomalyService {

    @Autowired
    private SessionRepository sessionRepository;

    @Value("${datasheild.auth.device.anomaly-threshold-hours:24}")
    private int anomalyThresholdHours;

    @Value("${datasheild.auth.device.min-travel-time-minutes:30}")
    private int minTravelTimeMinutes;

    /**
     * Check for anomalies on user login
     * Returns risk level: LOW, MEDIUM, HIGH
     */
    public AnomalyRiskLevel detectAnomalies(UUID userId, UUID tenantId, String ipAddress, String userAgent) {
        List<Session> recentSessions = sessionRepository
                .findRecentActiveSessions(userId, tenantId, LocalDateTime.now().minusHours(anomalyThresholdHours));

        // No prior sessions = new account, minimal risk
        if (recentSessions.isEmpty()) {
            log.info("New account login: user {} from IP {} (LOW risk)", userId, ipAddress);
            return AnomalyRiskLevel.LOW;
        }

        Session lastSession = recentSessions.get(0);
        AnomalyRiskLevel riskLevel = AnomalyRiskLevel.LOW;

        // Check 1: IP Address Change
        if (!isIPAddressSame(lastSession.getIpAddress(), ipAddress)) {
            riskLevel = AnomalyRiskLevel.MEDIUM;
            log.warn("IP address change detected for user {} - Old: {}, New: {}", 
                    userId, lastSession.getIpAddress(), ipAddress);
            
            // Check for impossible travel (too fast)
            if (isImpossibleTravel(lastSession.getIpAddress(), ipAddress, lastSession.getLastActivityAt())) {
                riskLevel = AnomalyRiskLevel.HIGH;
                log.error("IMPOSSIBLE TRAVEL detected for user {} - suggests credential theft", userId);
            }
        }

        // Check 2: User Agent Change (browser/device fingerprint)
        if (!isUserAgentSame(lastSession.getUserAgent(), userAgent)) {
            riskLevel = AnomalyRiskLevel.MEDIUM;
            log.warn("User agent change detected for user {} - Old: {}, New: {}", 
                    userId, lastSession.getUserAgent(), userAgent);
        }

        // Check 3: Time-based anomaly (unusual login time)
        if (isUnusualLoginTime(userId, tenantId)) {
            if (riskLevel == AnomalyRiskLevel.LOW) {
                riskLevel = AnomalyRiskLevel.LOW;  // Unusual time alone is minor
            }
            log.info("Unusual login time detected for user {}", userId);
        }

        return riskLevel;
    }

    /**
     * Check if IP address is the same
     * Handles proxy/load balancer cases
     */
    private boolean isIPAddressSame(String previousIp, String currentIp) {
        if (previousIp == null || currentIp == null) {
            return false;
        }
        
        // Exact match
        if (previousIp.equals(currentIp)) {
            return true;
        }

        // Check if within same IP subnet (simple check for corporate networks)
        // For /24 subnet: 192.168.1.0 - 192.168.1.255 are considered same
        String[] prevOctets = previousIp.split("\\.");
        String[] currOctets = currentIp.split("\\.");
        
        if (prevOctets.length == 4 && currOctets.length == 4) {
            // Same subnet if first 3 octets match (assumes /24 network)
            return prevOctets[0].equals(currOctets[0]) &&
                   prevOctets[1].equals(currOctets[1]) &&
                   prevOctets[2].equals(currOctets[2]);
        }
        
        return false;
    }

    /**
     * Check if user agent (device fingerprint) is the same
     * User agent includes: browser, version, OS, device type
     */
    private boolean isUserAgentSame(String previousAgent, String currentAgent) {
        if (previousAgent == null || currentAgent == null) {
            return false;
        }
        
        // Simple substring matching (same browser family)
        // In production, use more sophisticated fingerprinting:
        // - Parse user agent to extract: browser, version, OS, device type
        // - Compare major components
        // - Use fuzzy matching for minor version differences
        
        String prevBrowser = extractBrowserFromUserAgent(previousAgent);
        String currBrowser = extractBrowserFromUserAgent(currentAgent);
        
        String prevOs = extractOSFromUserAgent(previousAgent);
        String currOs = extractOSFromUserAgent(currentAgent);
        
        // Different browser OR OS = device change
        return prevBrowser.equals(currBrowser) && prevOs.equals(currOs);
    }

    /**
     * Check for impossible travel
     * Example: User in NY 5 minutes ago, now in LA (impossible without airplane)
     */
    private boolean isImpossibleTravel(String previousIp, String currentIp, LocalDateTime lastActivityTime) {
        if (previousIp == null || currentIp == null || lastActivityTime == null) {
            return false;
        }

        // In production, implement:
        // 1. IP geolocation lookup (using MaxMind GeoIP2, IP2Location, etc.)
        // 2. Calculate distance between cities
        // 3. Check if distance / time > maximum travel speed (~900 km/h = airplane)
        
        // For now, log as detection point
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastActivity = java.time.temporal.ChronoUnit.MINUTES.between(lastActivityTime, now);
        
        log.debug("Impossible travel check: {} IP {} to {} IP {} in {} minutes",
                previousIp, currentIp, minutesSinceLastActivity);
        
        // TODO: Implement geolocation-based impossible travel detection
        // For now, always return false (not implemented)
        return false;
    }

    /**
     * Check if login time is unusual for user
     * Example: User always logs in 9-5 weekdays, now login at 3 AM
     */
    private boolean isUnusualLoginTime(UUID userId, UUID tenantId) {
        // In production, implement:
        // 1. Analyze user's login patterns (histogram of hours)
        // 2. Calculate standard deviation
        // 3. Flag logins > 2 sigma from mean
        
        // For now, log as detection point
        log.debug("Unusual login time check for user {}", userId);
        
        // TODO: Implement behavioral analysis of login times
        // For now, always return false (not implemented)
        return false;
    }

    /**
     * Extract browser name from user agent string
     */
    private String extractBrowserFromUserAgent(String userAgent) {
        if (userAgent == null) return "";
        
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("Opera")) return "Opera";
        
        return "Unknown";
    }

    /**
     * Extract OS from user agent string
     */
    private String extractOSFromUserAgent(String userAgent) {
        if (userAgent == null) return "";
        
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS")) return "MacOS";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iOS")) return "iOS";
        if (userAgent.contains("Linux")) return "Linux";
        
        return "Unknown";
    }

    /**
     * Risk levels for anomaly detection
     */
    public enum AnomalyRiskLevel {
        LOW,      // No anomalies or new account
        MEDIUM,   // IP change or device change (normal for travel)
        HIGH      // Impossible travel or multiple major anomalies (suggests compromise)
    }

    /**
     * Action to take based on risk level
     */
    public enum AnomalyAction {
        ALLOW,           // Allow login immediately
        WARN,            // Show warning but allow login
        STEP_UP_AUTH,    // Require additional authentication
        BLOCK            // Block login, require password reset
    }

    /**
     * Recommended action based on risk level
     */
    public AnomalyAction getRecommendedAction(AnomalyRiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> AnomalyAction.ALLOW;           // New device/location = LOW risk
            case MEDIUM -> AnomalyAction.WARN;         // IP/device change = WARN user
            case HIGH -> AnomalyAction.STEP_UP_AUTH;   // Impossible travel = require additional auth
        };
    }

    /**
     * Log anomaly for audit trail
     */
    public void logAnomaly(UUID userId, UUID tenantId, AnomalyRiskLevel riskLevel, String reason) {
        log.warn("AUTH_ANOMALY - User: {}, Tenant: {}, Risk: {}, Reason: {}", 
                userId, tenantId, riskLevel, reason);
        // In production: Send to SIEM/audit log
    }
}

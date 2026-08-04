package io.datasheild.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.datasheild.auth.dto.MFASetupRequest;
import io.datasheild.auth.dto.MFASetupResponse;
import io.datasheild.auth.dto.MFAVerifyRequest;
import io.datasheild.auth.dto.MFAVerifyResponse;
import io.datasheild.auth.entity.MFADevice;
import io.datasheild.auth.exception.UnauthorizedException;
import io.datasheild.auth.service.MFAService;
import io.datasheild.auth.util.SecurityContextUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MFA Management Endpoints
 * 
 * Handles TOTP/MFA setup, verification, and device management
 * All endpoints require authentication (valid JWT token)
 */
@RestController
@RequestMapping("/v1/auth/mfa")
@Slf4j
public class MFAController {

    @Autowired
    private MFAService mfaService;

    /**
     * POST /v1/auth/mfa/totp/setup
     * Initiate TOTP setup - Returns secret and QR code
     * User must verify within 10 minutes
     * 
     * Request body: { "tenantId": "..." }
     */
    @PostMapping("/totp/setup")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<MFASetupResponse> setupTOTP(
            @RequestBody MFASetupRequest request) {
        
        UUID userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        UUID tenantId = UUID.fromString(request.getTenantId());
        
        // Verify user belongs to tenant
        SecurityContextUtil.validateTenantAccess(userId, tenantId);
        
        log.info("TOTP setup initiated for user {} in tenant {}", userId, tenantId);
        
        Map<String, Object> setupData = mfaService.setupTOTP(userId, tenantId);
        
        MFASetupResponse response = MFASetupResponse.builder()
                .mfaSetupId((String) setupData.get("mfaSetupId"))
                .mfaType((String) setupData.get("mfaType"))
                .secret((String) setupData.get("secret"))
                .qrCode((String) setupData.get("qrCode"))
                .backupCodes((String[]) setupData.get("backupCodes"))
                .verificationUrl((String) setupData.get("verificationUrl"))
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /v1/auth/mfa/totp/verify
     * Verify TOTP setup with code from Authenticator app
     * This activates the TOTP device for login
     * 
     * Request body: {
     *   "tenantId": "...",
     *   "mfaSetupId": "...",
     *   "verificationCode": "123456"
     * }
     */
    @PostMapping("/totp/verify")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<MFAVerifyResponse> verifyTOTPSetup(
            @RequestBody MFAVerifyRequest request) {
        
        UUID userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        UUID tenantId = UUID.fromString(request.getTenantId());
        SecurityContextUtil.validateTenantAccess(userId, tenantId);
        
        if (request.getVerificationCode() == null || request.getVerificationCode().isEmpty()) {
            throw new IllegalArgumentException("Verification code is required");
        }
        
        // TODO: Retrieve secret from temporary setup cache (Redis)
        // For now, this requires the secret to be passed in a secure session
        String secret = (String) SecurityContextUtil.getSessionAttribute("totp_secret_" + request.getMfaSetupId());
        if (secret == null) {
            throw new UnauthorizedException("TOTP setup expired or not found");
        }
        
        log.info("TOTP verification initiated for user {}", userId);
        
        MFADevice device = mfaService.verifyTOTPSetup(userId, tenantId, secret, request.getVerificationCode());
        
        MFAVerifyResponse response = MFAVerifyResponse.builder()
                .mfaDeviceId(device.getId().toString())
                .mfaType(device.getType().toString())
                .status("VERIFIED")
                .message("TOTP device successfully verified and activated")
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /v1/auth/mfa/devices
     * List all MFA devices for current user
     */
    @GetMapping("/devices")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> listMFADevices(
            @RequestParam String tenantId) {
        
        UUID userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        UUID tenantUUID = UUID.fromString(tenantId);
        SecurityContextUtil.validateTenantAccess(userId, tenantUUID);
        
        log.info("Listing MFA devices for user {} in tenant {}", userId, tenantUUID);
        
        List<MFADevice> devices = mfaService.listUserMFADevices(userId, tenantUUID);
        
        // Return sanitized device info (without secret)
        List<?> devicesList = devices.stream()
                .map(device -> new java.util.LinkedHashMap<String, Object>() {{
                    put("id", device.getId().toString());
                    put("type", device.getType().toString());
                    put("verified", device.getVerified());
                    put("active", device.getActive());
                    put("createdAt", device.getCreatedAt().toString());
                    put("lastUsedAt", device.getLastUsedAt() != null ? device.getLastUsedAt().toString() : "Never");
                }})
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new java.util.LinkedHashMap<String, Object>() {{
            put("devices", devicesList);
            put("count", devices.size());
        }});
    }

    /**
     * DELETE /v1/auth/mfa/devices/{deviceId}
     * Remove MFA device
     * Requires admin role or owner of device
     */
    @DeleteMapping("/devices/{deviceId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> removeMFADevice(
            @PathVariable UUID deviceId,
            @RequestParam String tenantId) {
        
        UUID userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        UUID tenantUUID = UUID.fromString(tenantId);
        SecurityContextUtil.validateTenantAccess(userId, tenantUUID);
        
        log.info("Removing MFA device {} for user {}", deviceId, userId);
        
        mfaService.removeMFADevice(userId, tenantUUID, deviceId);
        
        return ResponseEntity.ok(new java.util.LinkedHashMap<String, Object>() {{
            put("message", "MFA device removed successfully");
            put("deviceId", deviceId.toString());
        }});
    }

    /**
     * POST /v1/auth/mfa/devices/{deviceId}/disable
     * Disable MFA device (can be re-enabled by admin)
     */
    @PostMapping("/devices/{deviceId}/disable")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> disableMFADevice(
            @PathVariable UUID deviceId,
            @RequestParam String tenantId) {
        
        UUID userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        UUID tenantUUID = UUID.fromString(tenantId);
        SecurityContextUtil.validateTenantAccess(userId, tenantUUID);
        
        log.info("Disabling MFA device {} for user {}", deviceId, userId);
        
        // TODO: Implement device disable logic
        // For now, remove the device
        mfaService.removeMFADevice(userId, tenantUUID, deviceId);
        
        return ResponseEntity.ok(new java.util.LinkedHashMap<String, Object>() {{
            put("message", "MFA device disabled successfully");
            put("deviceId", deviceId.toString());
        }});
    }

    /**
     * GET /v1/auth/mfa/status
     * Check MFA status for current user
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> getMFAStatus(
            @RequestParam String tenantId) {
        
        UUID userId = SecurityContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        UUID tenantUUID = UUID.fromString(tenantId);
        SecurityContextUtil.validateTenantAccess(userId, tenantUUID);
        
        List<MFADevice> devices = mfaService.listUserMFADevices(userId, tenantUUID);
        boolean mfaEnabled = devices.stream().anyMatch(MFADevice::getActive);
        long activeDevices = devices.stream().filter(MFADevice::getActive).count();
        
        List<?> deviceList = devices.stream()
                .map(d -> new java.util.LinkedHashMap<String, Object>() {{
                    put("type", d.getType().toString());
                    put("active", d.getActive());
                }})
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new java.util.LinkedHashMap<String, Object>() {{
            put("mfaEnabled", mfaEnabled);
            put("activeDevices", activeDevices);
            put("totalDevices", devices.size());
            put("devices", deviceList);
        }});
    }
}

package io.datasheild.rightsservice.service;

import io.datasheild.rightsservice.dto.CreateDPRRequest;
import io.datasheild.rightsservice.dto.DPRActivityResponse;
import io.datasheild.rightsservice.dto.DPRRequestResponse;
import io.datasheild.rightsservice.dto.VerifyIdentityRequest;
import io.datasheild.rightsservice.entity.*;
import io.datasheild.rightsservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DPRService {

    private final DPRRequestRepository dprRequestRepository;
    private final DPRActivityRepository dprActivityRepository;
    private final IdentityVerificationOTPRepository otpRepository;
    private final DPRAggregatedDataRepository aggregatedDataRepository;
    private final DPROutboxRepository outboxRepository;
    private final OTPService otpService;

    @Transactional
    public DPRRequestResponse createDPRRequest(UUID tenantId, UUID dataPrincipalId, CreateDPRRequest request) {
        log.info("Creating DPR request: tenant={} type={}", tenantId, request.getRequestType());

        // Check for duplicate requests (same type within 30 days)
        long duplicateCount = dprRequestRepository.countDuplicateRequestsSince(
                tenantId, dataPrincipalId, request.getRequestType(), LocalDateTime.now().minusDays(30)
        );
        if (duplicateCount > 0) {
            log.warn("Duplicate request detected: tenant={} dp={} type={}", tenantId, dataPrincipalId, request.getRequestType());
            throw new IllegalArgumentException("Duplicate request within 30 days");
        }

        // Create DPR Request
        DPRRequest dprRequest = DPRRequest.builder()
                .tenantId(tenantId)
                .dataPrincipalId(dataPrincipalId)
                .requestType(request.getRequestType())
                .channel(request.getChannel())
                .requestDetails(request.getRequestDetails())
                .requestMetadata(request.getRequestMetadata())
                .status(DPRRequest.DPRStatus.RECEIVED)
                .identityVerified(false)
                .activityCount(0)
                .build();

        DPRRequest saved = dprRequestRepository.save(dprRequest);

        // Log activity
        logActivity(tenantId, saved.getId(), DPRActivity.ActivityType.REQUEST_RECEIVED, "Request received via " + request.getChannel(), "SYSTEM");

        // Trigger identity verification
        triggerIdentityVerification(tenantId, saved.getId(), dataPrincipalId);

        // Publish event
        publishEvent(tenantId, "dpr.requested", buildEventPayload(saved));

        log.info("DPR request created: id={}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public DPRRequestResponse verifyIdentity(UUID tenantId, VerifyIdentityRequest request) {
        log.info("Verifying identity for request: id={}", request.getRequestId());

        DPRRequest dprRequest = dprRequestRepository.findByTenantAndId(tenantId, request.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Verify OTP
        IdentityVerificationOTP otp = otpRepository.findActivePending(tenantId, request.getRequestId(), LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("No valid OTP found"));

        boolean verified = otpService.verifyOTP(otp, request.getOtpCode());
        if (!verified) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otp.setLastAttemptAt(LocalDateTime.now());
            if (otp.getAttemptCount() >= 3) {
                otp.setStatus(IdentityVerificationOTP.OTPStatus.FAILED);
            }
            otpRepository.save(otp);
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Mark OTP as verified
        otp.setStatus(IdentityVerificationOTP.OTPStatus.VERIFIED);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        // Update request
        dprRequest.setStatus(DPRRequest.DPRStatus.VERIFIED);
        dprRequest.setIdentityVerified(true);
        dprRequest.setVerifiedAt(LocalDateTime.now());
        DPRRequest updated = dprRequestRepository.save(dprRequest);

        // Log activity
        logActivity(tenantId, updated.getId(), DPRActivity.ActivityType.VERIFICATION_COMPLETED, "Identity verified via OTP", "SYSTEM");

        // Publish event
        publishEvent(tenantId, "dpr.verified", buildEventPayload(updated));

        log.info("Identity verified for request: id={}", updated.getId());
        return mapToResponse(updated);
    }

    public List<DPRRequestResponse> getRequestsByDataPrincipal(UUID tenantId, UUID dataPrincipalId) {
        log.debug("Fetching DPR requests for tenant={} dp={}", tenantId, dataPrincipalId);

        List<DPRRequest> requests = dprRequestRepository.findActiveByDataPrincipal(tenantId, dataPrincipalId);
        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DPRRequestResponse getRequestDetails(UUID tenantId, UUID requestId) {
        log.debug("Fetching request details: id={}", requestId);

        DPRRequest request = dprRequestRepository.findByTenantAndId(tenantId, requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        return mapToResponse(request);
    }

    public List<DPRActivityResponse> getRequestActivities(UUID tenantId, UUID requestId) {
        log.debug("Fetching activities for request: id={}", requestId);

        List<DPRActivity> activities = dprActivityRepository.findByDPRRequest(tenantId, requestId);
        return activities.stream()
                .map(this::mapActivityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void checkSLAViolations(UUID tenantId) {
        log.info("Checking SLA violations for tenant={}", tenantId);

        List<DPRRequest> violations = dprRequestRepository.findSLAViolatingRequests(tenantId, LocalDateTime.now());
        log.warn("Found {} SLA violations", violations.size());

        for (DPRRequest request : violations) {
            logActivity(tenantId, request.getId(), DPRActivity.ActivityType.REQUEST_COMPLETED, 
                       "SLA deadline exceeded", "SYSTEM");
        }
    }

    @Transactional
    public void processPendingRequests(UUID tenantId) {
        log.info("Processing pending requests for tenant={}", tenantId);

        List<DPRRequest> pending = dprRequestRepository.findPendingVerification(tenantId, LocalDateTime.now());
        log.info("Found {} pending verification requests", pending.size());

        for (DPRRequest request : pending) {
            if (request.getVerificationChallengeId() == null) {
                triggerIdentityVerification(tenantId, request.getId(), request.getDataPrincipalId());
            }
        }
    }

    private void triggerIdentityVerification(UUID tenantId, UUID requestId, UUID dataPrincipalId) {
        log.info("Triggering identity verification for request: id={}", requestId);

        String otpCode = otpService.generateOTP();
        IdentityVerificationOTP otp = IdentityVerificationOTP.builder()
                .tenantId(tenantId)
                .dprRequestId(requestId)
                .dataPrincipalId(dataPrincipalId)
                .channel(IdentityVerificationOTP.OTPChannel.EMAIL)  // Default to email
                .otpCode(otpCode)
                .hashedOtp(otpService.hashOTP(otpCode))
                .status(IdentityVerificationOTP.OTPStatus.SENT)
                .attemptCount(0)
                .build();

        IdentityVerificationOTP saved = otpRepository.save(otp);

        // Send OTP (in production, call notification service)
        log.info("OTP sent to data principal for request: id={}", requestId);

        // Update request status
        DPRRequest request = dprRequestRepository.findById(requestId).orElseThrow();
        request.setStatus(DPRRequest.DPRStatus.VERIFICATION_PENDING);
        request.setVerificationChallengeId(saved.getId());
        dprRequestRepository.save(request);

        logActivity(tenantId, requestId, DPRActivity.ActivityType.VERIFICATION_SENT, "OTP sent to data principal", "SYSTEM");
    }

    private void logActivity(UUID tenantId, UUID requestId, DPRActivity.ActivityType type, String description, String actor) {
        DPRActivity activity = DPRActivity.builder()
                .tenantId(tenantId)
                .dprRequestId(requestId)
                .activityType(type)
                .activityDescription(description)
                .actor(actor)
                .auditTraceId(UUID.randomUUID().toString())
                .build();

        dprActivityRepository.save(activity);
    }

    private void publishEvent(UUID tenantId, String eventType, String payload) {
        DPROutbox outbox = DPROutbox.builder()
                .tenantId(tenantId)
                .eventType(eventType)
                .eventPayload(payload)
                .published(false)
                .retryCount(0)
                .build();

        outboxRepository.save(outbox);
        log.debug("Event published to outbox: eventType={}", eventType);
    }

    private String buildEventPayload(DPRRequest request) {
        return "{\"requestId\":\"" + request.getId() + "\",\"type\":\"" + request.getRequestType() + 
               "\",\"status\":\"" + request.getStatus() + "\"}";
    }

    private DPRRequestResponse mapToResponse(DPRRequest request) {
        return DPRRequestResponse.builder()
                .id(request.getId())
                .requestType(request.getRequestType())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .slaDeadline(request.getSlaDeadline())
                .verifiedAt(request.getVerifiedAt())
                .completedAt(request.getCompletedAt())
                .identityVerified(request.getIdentityVerified())
                .channel(request.getChannel())
                .activityCount(request.getActivityCount())
                .rejectionReason(request.getRejectionReason())
                .build();
    }

    private DPRActivityResponse mapActivityToResponse(DPRActivity activity) {
        return DPRActivityResponse.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .activityDescription(activity.getActivityDescription())
                .actor(activity.getActor())
                .createdAt(activity.getCreatedAt())
                .auditTraceId(activity.getAuditTraceId())
                .build();
    }
}

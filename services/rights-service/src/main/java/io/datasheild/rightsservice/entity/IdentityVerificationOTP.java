package io.datasheild.rightsservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "identity_verification_otp", schema = "rights",
       indexes = {
           @Index(name = "idx_otp_request", columnList = "dpr_request_id"),
           @Index(name = "idx_otp_status", columnList = "status"),
           @Index(name = "idx_otp_expires", columnList = "expires_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityVerificationOTP {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID dprRequestId;

    @Column(nullable = false)
    private UUID dataPrincipalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OTPChannel channel = OTPChannel.EMAIL;

    @Column(nullable = false, length = 6)
    private String otpCode;

    @Column(nullable = false)
    private String hashedOtp;  // SHA-256 hash of OTP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OTPStatus status = OTPStatus.SENT;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private Integer attemptCount;

    @Column
    private LocalDateTime lastAttemptAt;

    public enum OTPChannel {
        EMAIL,
        SMS,
        WHATSAPP
    }

    public enum OTPStatus {
        SENT,          // OTP sent to user
        VERIFIED,      // OTP verified successfully
        EXPIRED,       // OTP expired
        FAILED         // Max attempts exceeded
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = OTPStatus.SENT;
        }
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusMinutes(15);  // 15-min OTP expiry
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }
}

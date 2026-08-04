package io.datasheild.auth.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", schema = "auth", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"tenant_id", "email"}),
           @UniqueConstraint(columnNames = {"tenant_id", "username"})
       },
       indexes = {
           @Index(name = "idx_user_tenant_email", columnList = "tenant_id,email"),
           @Index(name = "idx_user_tenant_username", columnList = "tenant_id,username"),
           @Index(name = "idx_user_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Column(length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MFAMethod preferredMfaMethod = MFAMethod.TOTP;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", 
                    joinColumns = @JoinColumn(name = "user_id"),
                    schema = "auth")
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastLoginAt;

    @Column
    private LocalDateTime passwordChangedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
    }

    public enum MFAMethod {
        TOTP, SMS, EMAIL
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }
}

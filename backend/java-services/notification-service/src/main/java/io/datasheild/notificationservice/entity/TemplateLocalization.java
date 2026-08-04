package io.datasheild.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "template_localizations",
    schema = "notification",
    indexes = {
        @Index(name = "idx_template_lang", columnList = "template_id,language_code"),
        @Index(name = "idx_language_code", columnList = "language_code")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateLocalization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID templateId;

    @Column(nullable = false, length = 5)
    private String languageCode;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isValid() {
        return templateId != null && languageCode != null && !languageCode.isBlank() &&
               subject != null && !subject.isBlank() && body != null && !body.isBlank();
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}

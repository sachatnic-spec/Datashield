package io.datasheild.notificationservice.repository;

import io.datasheild.notificationservice.entity.TemplateLocalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateLocalizationRepository extends JpaRepository<TemplateLocalization, UUID> {

    @Query("SELECT tl FROM TemplateLocalization tl WHERE tl.templateId = :templateId AND tl.languageCode = :lang")
    Optional<TemplateLocalization> findByTemplateAndLanguage(@Param("templateId") UUID templateId, @Param("lang") String lang);

    @Query("SELECT tl FROM TemplateLocalization tl WHERE tl.templateId = :templateId ORDER BY tl.languageCode")
    List<TemplateLocalization> findByTemplate(@Param("templateId") UUID templateId);

    @Query("SELECT tl FROM TemplateLocalization tl WHERE tl.templateId = :templateId AND tl.languageCode IN :languages")
    List<TemplateLocalization> findByTemplateAndLanguages(@Param("templateId") UUID templateId, @Param("languages") List<String> languages);
}

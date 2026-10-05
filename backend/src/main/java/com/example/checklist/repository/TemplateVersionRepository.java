package com.example.checklist.repository;

import com.example.checklist.entity.TemplateVersion;
import com.example.checklist.enums.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {
    /** 找某範本目前發布中的版本（傳 PUBLISHED）；同時只會有一個，由 service 保證 */
    Optional<TemplateVersion> findByTemplateIdAndStatus(Long templateId, VersionStatus status);

    /** 版本歷史，新的在前 */
    List<TemplateVersion> findByTemplateIdOrderByVersionNoDesc(Long templateId);

    /** 建立新版本時，取最大版號再 +1 */
    Optional<TemplateVersion> findTopByTemplateIdOrderByVersionNoDesc(Long templateId);
}

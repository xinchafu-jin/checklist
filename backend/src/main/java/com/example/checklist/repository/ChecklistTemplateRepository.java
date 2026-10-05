package com.example.checklist.repository;

import com.example.checklist.entity.ChecklistTemplate;
import com.example.checklist.enums.TemplateKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {
    /** 新增範本時擋重複編號 */
    boolean existsByCode(String code);

    /** 範本清單，依類型（定期檢點／作業許可） */
    List<ChecklistTemplate> findByKindOrderByCodeAsc(TemplateKind kind);
}

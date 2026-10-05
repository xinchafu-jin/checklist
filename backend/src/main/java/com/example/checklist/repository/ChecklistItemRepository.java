package com.example.checklist.repository;

import com.example.checklist.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    /** 載入某版本的所有項目，依順序 */
    List<ChecklistItem> findByVersionIdOrderBySeqAsc(Long versionId);
}

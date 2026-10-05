package com.example.checklist.repository;

import com.example.checklist.entity.ChecklistItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChecklistItemOptionRepository extends JpaRepository<ChecklistItemOption, Long> {
    /** 一次載入整個版本所有項目的選項，避免每個項目各查一次 */
    List<ChecklistItemOption> findByItemIdInOrderByItemIdAscSeqAsc(Collection<Long> itemIds);
}

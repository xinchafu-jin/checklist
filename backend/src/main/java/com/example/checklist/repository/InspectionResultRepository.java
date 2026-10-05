package com.example.checklist.repository;

import com.example.checklist.entity.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InspectionResultRepository extends JpaRepository<InspectionResult, Long> {
    /** 查看一筆紀錄的逐項結果 */
    List<InspectionResult> findByRecordId(Long recordId);

    /** 一次載入多筆紀錄的結果，例如月表 */
    List<InspectionResult> findByRecordIdIn(Collection<Long> recordIds);
}

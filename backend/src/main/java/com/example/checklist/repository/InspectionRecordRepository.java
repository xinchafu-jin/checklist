package com.example.checklist.repository;

import com.example.checklist.entity.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, Long> {
    /** 送出前先檢查這一期是否已經檢過；資料庫的 uk_record_period 是最後防線 */
    boolean existsByEquipmentIdAndTemplateIdAndPeriodKey(Long equipmentId, Long templateId, String periodKey);

    /** 計算今日待檢點：把今天涉及的期間一次查出來，比對哪些已經完成 */
    List<InspectionRecord> findByPeriodKeyIn(Collection<String> periodKeys);

    /** 紀錄查詢：依檢點日區間 */
    List<InspectionRecord> findByBusinessDateBetweenOrderByBusinessDateDescSubmittedAtDesc(LocalDate from, LocalDate to);

    /** 某台設備一段期間的紀錄，例如畫月表 */
    List<InspectionRecord> findByEquipmentIdAndBusinessDateBetweenOrderByBusinessDateAsc(Long equipmentId, LocalDate from, LocalDate to);
}

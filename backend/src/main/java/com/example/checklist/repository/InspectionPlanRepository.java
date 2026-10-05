package com.example.checklist.repository;

import com.example.checklist.entity.InspectionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionPlanRepository extends JpaRepository<InspectionPlan, Long> {
    /** 計算今日待檢點：所有啟用中的排程 */
    List<InspectionPlan> findByActiveTrue();

    /** 某台設備套用了哪些範本 */
    List<InspectionPlan> findByEquipmentId(Long equipmentId);

    /** 新增排程時擋重複（同設備同範本只能一個） */
    boolean existsByEquipmentIdAndTemplateId(Long equipmentId, Long templateId);
}

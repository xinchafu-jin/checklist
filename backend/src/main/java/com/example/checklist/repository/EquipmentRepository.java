package com.example.checklist.repository;

import com.example.checklist.entity.Equipment;
import com.example.checklist.enums.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    /** 新增設備時擋重複編號 */
    boolean existsByCode(String code);

    /** 用設備編號找設備（之後掃 QR Code 也用這個） */
    Optional<Equipment> findByCode(String code);

    /** 依區域列出設備 */
    List<Equipment> findByAreaIdOrderByCodeAsc(Long areaId);

    /** 依狀態列出設備，例如只看使用中 */
    List<Equipment> findByStatusOrderByCodeAsc(EquipmentStatus status);
}

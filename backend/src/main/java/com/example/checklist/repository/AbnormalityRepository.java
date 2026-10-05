package com.example.checklist.repository;

import com.example.checklist.entity.Abnormality;
import com.example.checklist.enums.AbnormalityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AbnormalityRepository extends JpaRepository<Abnormality, Long> {
    /** 異常追蹤：未結案（OPEN、IN_PROGRESS）的單，舊的在前 */
    List<Abnormality> findByStatusInOrderByCreatedAtAsc(Collection<AbnormalityStatus> statuses);

    /** 某台設備的異常歷史 */
    List<Abnormality> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);

    /** 儀表板：未結異常數 */
    long countByStatusIn(Collection<AbnormalityStatus> statuses);
}

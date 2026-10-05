package com.example.checklist.repository;

import com.example.checklist.entity.InspectionResultOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InspectionResultOptionRepository extends JpaRepository<InspectionResultOption, Long> {
    /** 一次載入多個結果選了哪些選項 */
    List<InspectionResultOption> findByResultIdIn(Collection<Long> resultIds);
}

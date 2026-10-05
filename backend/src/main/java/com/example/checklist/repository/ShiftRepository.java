package com.example.checklist.repository;

import com.example.checklist.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    /** 計算每班檢點：今天有哪些班 */
    List<Shift> findByActiveTrueOrderBySeqAsc();
}

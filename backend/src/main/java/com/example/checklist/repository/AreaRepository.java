package com.example.checklist.repository;

import com.example.checklist.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {
    /** 新增、改名時擋重複 */
    boolean existsByName(String name);

    /** 下拉選單、依區域篩選設備 */
    List<Area> findAllByOrderByNameAsc();
}

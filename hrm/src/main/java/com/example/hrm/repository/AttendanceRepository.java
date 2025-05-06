package com.example.hrm.repository;

import com.example.hrm.domain.ChamCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<ChamCong, Integer> {
    ChamCong findById(int id);
    void deleteById(Integer id);
    ChamCong save(ChamCong chamCong);
    List<ChamCong> findAll();
}

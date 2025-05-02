package com.example.hrm.repository;

import com.example.hrm.domain.NghiPhep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<NghiPhep, Integer> {
    NghiPhep findById(int id);
    void deleteById(Integer id);
    List<NghiPhep> findAll();
}

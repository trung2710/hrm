package com.example.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hrm.domain.PhongBan;


@Repository
public interface  DepartmentRepository extends JpaRepository<PhongBan, Long>{
    PhongBan findById(long id);
}

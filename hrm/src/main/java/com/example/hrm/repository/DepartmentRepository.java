package com.example.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hrm.domain.PhongBan;

import java.util.List;


@Repository
public interface  DepartmentRepository extends JpaRepository<PhongBan, Integer>{
    PhongBan findById(int id);
    List<PhongBan>findAll();
    void deleteById(Integer id);
    PhongBan findByTenPhongBan(String name);
}

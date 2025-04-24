package com.example.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.hrm.domain.NhanVien;


@Repository
public interface  UserRepository extends JpaRepository<NhanVien, Long>{
    NhanVien findById(long id);
    
}

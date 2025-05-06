package com.example.hrm.repository;

import com.example.hrm.domain.Luong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Luong, Integer> {
    Luong findById(int id);
    void deleteById(Integer id);
    List<Luong> findAll();
}

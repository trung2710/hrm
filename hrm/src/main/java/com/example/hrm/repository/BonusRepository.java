package com.example.hrm.repository;

import com.example.hrm.domain.Thuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusRepository extends JpaRepository<Thuong, Integer> {
    List<Thuong> findAll();
    void deleteById(Integer id);
    Thuong findById(int id);
}

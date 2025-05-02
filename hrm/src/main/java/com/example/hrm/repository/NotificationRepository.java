package com.example.hrm.repository;

import com.example.hrm.domain.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<ThongBao, Integer> {
    ThongBao findById(int id);
    void deleteById(Integer id);
    List<ThongBao> findAll();
}

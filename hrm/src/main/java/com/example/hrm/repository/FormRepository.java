package com.example.hrm.repository;

import com.example.hrm.domain.DonTu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormRepository extends JpaRepository<DonTu, Integer> {
    DonTu findById(int id);
    void deleteById(Integer id);
    @Query(value="SELECT d FROM DonTu d WHERE d.loaiDon= : name")
    DonTu findByName(@Param("name") String name);
    List<DonTu> findAll();
}

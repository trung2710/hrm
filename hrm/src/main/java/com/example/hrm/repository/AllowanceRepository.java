package com.example.hrm.repository;

import com.example.hrm.domain.PhuCap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllowanceRepository extends JpaRepository<PhuCap, Integer> {
    @Query(value="SELECT * FROM PhuCap", nativeQuery = true)
    List<PhuCap> findAllAllowances();
    PhuCap findById(int id);
    void deleteById(Integer id);
    @Query(value="SELECT p FROM PhuCap p WHERE p.loaiPhuCap= :name", nativeQuery = true)
    PhuCap findByName(@Param("name") String name);
}

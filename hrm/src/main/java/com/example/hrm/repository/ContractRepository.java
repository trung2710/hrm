package com.example.hrm.repository;

import com.example.hrm.domain.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<HopDong, Integer> {
    List<HopDong> findAll();
    void deleteById(Integer id);
    HopDong findById(int id);
    @Query(value="SELECT h FROM HopDong h WHERE h.loaiHopDong= : name")
    HopDong findByName(@Param("name") String name);
}

package com.example.hrm.repository;

import com.example.hrm.domain.ViPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViolationRepository extends JpaRepository<ViPham, Integer>{
    ViPham findById(int id);
    void deleteById(Integer id);
    @Query(value="SELECT * FROM ViPham WHERE tenViPham= :name", nativeQuery = true)
    ViPham findByName(@Param("name") String name);
    List<ViPham>findAll();
}

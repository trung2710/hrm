package com.example.hrm.repository;

import com.example.hrm.domain.Luong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRepository extends JpaRepository<Luong, Integer>, JpaSpecificationExecutor<Luong> {
    Luong findById(int id);
    void deleteById(Integer id);
    List<Luong> findAll();
    @Query(value="SELECT * FROM Luong", nativeQuery = true)
    Page<Luong> findAllWithPagination(Pageable pageable);
    Page<Luong> findAll(Specification<Luong> spec, Pageable pageable);
}

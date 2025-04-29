package com.example.hrm.repository;

import com.example.hrm.domain.DuAn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectReppository extends JpaRepository<DuAn, Integer> {
    @Query(value="SELECT * FROM DuAn", nativeQuery = true)
    List<DuAn> findAllProjects();
    @Query(value = "SELECT d FROM DuAn d WHERE d.tenDuAn = :name", nativeQuery = true)
    DuAn findByName(@Param("name") String name);
    void deleteById(Integer id);
}

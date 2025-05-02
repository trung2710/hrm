package com.example.hrm.repository;

import com.example.hrm.domain.BaoHiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InsuranceRepository extends JpaRepository<BaoHiem, Integer> {
    BaoHiem findById(int id);
    void deleteById(Integer id);
    List<BaoHiem> findAll();
    @Query(value="SELECT b FROM BaoHiem b WHERE b.nhanVien.id=:id AND b.tenBaoHiem=:name AND b.ngayHetHan>:now")
    BaoHiem findByName(@Param("id") int id, @Param("name") String name, @Param("now") LocalDate now);
}

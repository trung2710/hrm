package com.example.hrm.repository;

import com.example.hrm.domain.ChamCong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<ChamCong, Integer>, JpaSpecificationExecutor<ChamCong> {
    ChamCong findById(int id);
    void deleteById(Integer id);
    ChamCong save(ChamCong chamCong);
    List<ChamCong> findAll();
    ChamCong findByNhanVienIdAndNgay(Integer nhanVienId, LocalDate ngay);
    @Query("SELECT c FROM ChamCong c WHERE c.nhanVien.id = :id AND MONTH(c.ngay) = :month AND YEAR(c.ngay) = :year")
    List<ChamCong> findByMonthYear(@Param("id") int id, @Param("month") int month, @Param("year") int year);
    Page<ChamCong> findAll(Specification<ChamCong> specification, Pageable pageable);
}

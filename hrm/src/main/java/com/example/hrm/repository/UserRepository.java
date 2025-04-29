package com.example.hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hrm.domain.NhanVien;


@Repository
public interface  UserRepository extends JpaRepository<NhanVien, Long>{
    NhanVien findById(long id);
    @Query(value="SELECT n FROM NHANVIEN n WHERE n.hoTen= :name", nativeQuery = true)
    NhanVien findByName(@Param("name") String name);
    void deleteById(Long id);
}

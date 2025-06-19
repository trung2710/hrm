package com.example.hrm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.hrm.domain.NhanVien;

import java.util.List;


@Repository
public interface  UserRepository extends JpaRepository<NhanVien, Long>, JpaSpecificationExecutor<NhanVien> {
    NhanVien findById(long id);
    @Query(value="SELECT n FROM NHANVIEN n WHERE n.hoTen= :name", nativeQuery = true)
    NhanVien findByName(@Param("name") String name);
    void deleteById(Long id);
    NhanVien findByEmail(String email);
    List<NhanVien> findAll();
    @Query(value="SELECT * FROM NhanVien  WHERE email=:email",nativeQuery = true)
    NhanVien findUserByEmail(@Param("email") String email);
    Page<NhanVien> findAll(Specification<NhanVien> spec, Pageable pageable);
}

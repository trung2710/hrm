package com.example.hrm.repository;

import com.example.hrm.domain.NV_Thuong;
import com.example.hrm.domain.idClass.NVThuongId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NV_ThuongRepository extends JpaRepository<NV_Thuong,NVThuongId> {
    List<NV_Thuong> findAll();
    Optional<NV_Thuong> findById(NVThuongId id);
    void deleteById(NVThuongId id);

}

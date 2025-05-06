package com.example.hrm.repository;

import com.example.hrm.domain.NV_ViPham;
import com.example.hrm.domain.ViPham;
import com.example.hrm.domain.idClass.NVViPhamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NV_ViolationRepository extends JpaRepository<NV_ViPham, NVViPhamId> {
    Optional<NV_ViPham> findById(NVViPhamId id);
    void deleteById(NVViPhamId id);
    List<NV_ViPham> findAll();
}

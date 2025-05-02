package com.example.hrm.repository;

import com.example.hrm.domain.NV_PhuCap;
import com.example.hrm.domain.idClass.NVPhuCapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NV_AllowanceRepository extends JpaRepository<NV_PhuCap, NVPhuCapId> {
    List<NV_PhuCap> findAll();
    Optional<NV_PhuCap> findById(NVPhuCapId id);
    void deleteById(NVPhuCapId id);
}

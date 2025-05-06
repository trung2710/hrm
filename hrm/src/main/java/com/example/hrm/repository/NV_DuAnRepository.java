package com.example.hrm.repository;

import com.example.hrm.domain.NV_DuAn;
import com.example.hrm.domain.idClass.NVDuAnId;
import com.example.hrm.domain.idClass.NVViPhamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface NV_DuAnRepository extends JpaRepository<NV_DuAn, NVDuAnId> {
    Optional<NV_DuAn> findById(NVDuAnId id);
    void deleteById(NVDuAnId id);
    List<NV_DuAn> findAll();
}

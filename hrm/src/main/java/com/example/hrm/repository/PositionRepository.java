package com.example.hrm.repository;

import com.example.hrm.domain.ChucVu;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.Position;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<ChucVu, Integer> {
    @Query(value = "SELECT * FROM ChucVu", nativeQuery = true)
    List<ChucVu> findAllPositions();

    @Query(value = "SELECT * FROM ChucVu WHERE id = :id", nativeQuery = true)
    ChucVu findById(@Param("id") int id);

    @Query(value = "SELECT c FROM ChucVu c WHERE c.tenChucVu = :name ")
    ChucVu findByName(@Param("name") String name);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ChucVu WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") int id);

    @Query(value="SELECT c FROM ChucVu c WHERE c.phongBan.id=:id", nativeQuery = true)
    List<ChucVu> findByPhongBanId(@Param("id") int id);
}

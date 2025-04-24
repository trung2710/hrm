package com.example.hrm.domain;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name="BaoHiem")
public class BaoHiem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBaoHiem")
    private long id;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @Column(name = "TenBaoHiem")
    private String tenBaoHiem;

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "NgayHetHan", nullable = false)
    private LocalDate ngayHetHan;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getTenBaoHiem() {
        return tenBaoHiem;
    }

    public void setTenBaoHiem(String tenBaoHiem) {
        this.tenBaoHiem = tenBaoHiem;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDate ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }
}

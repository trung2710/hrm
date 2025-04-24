package com.example.hrm.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="PhuCap")
public class PhuCap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhuCap")
    private long id;
    @Column(name = "TenPhuCap", nullable = false, unique = true)
    private String tenPhuCap;
    @Column(name = "MucTien", nullable = false)
    private Double mucTien;

    @Column(name = "MoTa")
    private String moTa;

    public String getLoaiPhuCap() {
        return loaiPhuCap;
    }

    public void setLoaiPhuCap(String loaiPhuCap) {
        this.loaiPhuCap = loaiPhuCap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTenPhuCap() {
        return tenPhuCap;
    }

    public void setTenPhuCap(String tenPhuCap) {
        this.tenPhuCap = tenPhuCap;
    }

    public Double getMucTien() {
        return mucTien;
    }

    public void setMucTien(Double mucTien) {
        this.mucTien = mucTien;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    @Column(name = "LoaiPhuCap", nullable = false)
    private String loaiPhuCap;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;
}

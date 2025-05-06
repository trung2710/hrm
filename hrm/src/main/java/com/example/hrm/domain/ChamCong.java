package com.example.hrm.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
@Table(name="ChamCong")
public class ChamCong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChamCong")
    private long id;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien", referencedColumnName = "MaNhanVien")
    private NhanVien nhanVien;

    @Column(name = "Ngay", nullable = false)
    private LocalDate ngay;
    @Column(name = "GioVao")
    private LocalTime gioVao;
    @Column(name = "GioRa")
    private LocalTime gioRa;

    @Column(name = "TrangThai")
    private String trangThai;
    @Column(name = "SoGioTangCa", nullable = false, columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private Double soGioTangCa = 0.0;

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

    public LocalDate getNgay() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        this.ngay = ngay;
    }

    public LocalTime getGioVao() {
        return gioVao;
    }

    public void setGioVao(LocalTime gioVao) {
        this.gioVao = gioVao;
    }

    public LocalTime getGioRa() {
        return gioRa;
    }

    public void setGioRa(LocalTime gioRa) {
        this.gioRa = gioRa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Double getSoGioTangCa() {
        return soGioTangCa;
    }

    public void setSoGioTangCa(Double soGioTangCa) {
        this.soGioTangCa = soGioTangCa;
    }
}

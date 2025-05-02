package com.example.hrm.domain;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="NghiPhep")
public class NghiPhep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNghiPhep")
    private long id;
    
    @ManyToOne
    @JoinColumn(name = "MaNhanVien", nullable = false)
    private NhanVien nhanVien;
    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;
    @Column(name = "NgayKetThuc", nullable = false)
    private LocalDate ngayKetThuc;
    @Column(name = "LyDo", nullable = false)
    @NotNull(message="Vui long dien li do xin nghi")
    private String liDo;
    @Column(name = "TrangThaiPheDuyet")
    private String trangThaiPheDuyet = "Chờ duyệt";

    @Transient
    private Long soNgay;

    public Long getSoNgay() {
        return soNgay;
    }

    public void setSoNgay(Long soNgay) {
        this.soNgay = soNgay;
    }

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

    public String getLiDo() {
        return liDo;
    }

    public void setLiDo(String liDo) {
        this.liDo = liDo;
    }

    public String getTrangThaiPheDuyet() {
        return trangThaiPheDuyet;
    }

    public void setTrangThaiPheDuyet(String trangThaiPheDuyet) {
        this.trangThaiPheDuyet = trangThaiPheDuyet;
    }
}

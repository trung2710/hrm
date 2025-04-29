package com.example.hrm.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="NhanVien_Thuong")
public class NV_Thuong {
    @Id
    @ManyToOne
    @JoinColumn(name = "MaNhanVien", referencedColumnName = "MaNhanVien")
    private NhanVien nhanVien;
    
    @Id
    @ManyToOne
    @JoinColumn(name = "MaThuong", referencedColumnName = "MaThuong")
    private Thuong thuong;

    @Column(name = "NgayThuong")
    private LocalDate ngayThuong;

    @Column(name = "MucTien", nullable = false)
    private BigDecimal mucTien;

    public BigDecimal getMucTien() {
        return mucTien;
    }

    public void setMucTien(BigDecimal mucTien) {
        this.mucTien = mucTien;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public Thuong getThuong() {
        return thuong;
    }

    public void setThuong(Thuong thuong) {
        this.thuong = thuong;
    }

    public LocalDate getNgayThuong() {
        return ngayThuong;
    }

    public void setNgayThuong(LocalDate ngayThuong) {
        this.ngayThuong = ngayThuong;
    }
}

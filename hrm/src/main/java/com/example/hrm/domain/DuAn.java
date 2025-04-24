package com.example.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="DuAn")
public class DuAn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDuAn")
    private long id;
    @NotNull@Column(name = "TenDuAn", nullable = false)
    private String tenDuAn;
    @NotNull@Column(name = "TenKhachHang", nullable = false)
    private String tenKhachHang;
    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;
    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    @ManyToOne
    @JoinColumn(name = "MaPhongBan")
    private PhongBan phongBan;
    @Column(name = "PhiDuAn", nullable = false)
    private BigDecimal phiDuAn;

    @Column(name = "NganSach", nullable = false)
    private BigDecimal nganSach;

    @Column(name = "DoanhThu")
    private BigDecimal doanhThu;

    @Column(name = "TrangThai", nullable = false)
    private String trangThai = "Đang triển khai";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTenDuAn() {
        return tenDuAn;
    }

    public void setTenDuAn(String tenDuAn) {
        this.tenDuAn = tenDuAn;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
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

    public PhongBan getPhongBan() {
        return phongBan;
    }

    public void setPhongBan(PhongBan phongBan) {
        this.phongBan = phongBan;
    }

    public BigDecimal getPhiDuAn() {
        return phiDuAn;
    }

    public void setPhiDuAn(BigDecimal phiDuAn) {
        this.phiDuAn = phiDuAn;
    }

    public BigDecimal getNganSach() {
        return nganSach;
    }

    public void setNganSach(BigDecimal nganSach) {
        this.nganSach = nganSach;
    }

    public BigDecimal getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(BigDecimal doanhThu) {
        this.doanhThu = doanhThu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

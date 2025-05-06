package com.example.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name="Luong")
public class Luong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBangLuong")
    private long id;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien", referencedColumnName = "MaNhanVien")
    private NhanVien nhanVien;
    @Column(name = "Thang")
    private Integer thang;
    @Column(name = "Nam")
    private Integer nam;
    @Column(name = "TienTangCa", columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal tienTangCa;
    @Column(name = "SoNguoiPhuThuoc", columnDefinition = "INT DEFAULT 0")
    private Integer soNguoiPhuThuoc = 0;

    @Column(name = "ThueThuNhap", nullable = false)
    private BigDecimal thueThuNhap;

    @Transient
    private BigDecimal tienThuong;
    @Transient
    private BigDecimal tienPC;
    @Transient
    private BigDecimal tienViPham;

    public BigDecimal getTienViPham() {
        return tienViPham;
    }

    public void setTienViPham(BigDecimal tienViPham) {
        this.tienViPham = tienViPham;
    }

    public BigDecimal getTienThuong() {
        return tienThuong;
    }

    public void setTienThuong(BigDecimal tienThuong) {
        this.tienThuong = tienThuong;
    }

    public BigDecimal getTienPC() {
        return tienPC;
    }

    public void setTienPC(BigDecimal tienPC) {
        this.tienPC = tienPC;
    }

    @Column(name = "TongThuNhap", nullable = false)
    private BigDecimal tongThuNhap;
    @Column(name = "NgayNhan")
    private LocalDate ngayNhan;


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

    public Integer getThang() {
        return thang;
    }

    public void setThang(Integer thang) {
        this.thang = thang;
    }

    public Integer getNam() {
        return nam;
    }

    public void setNam(Integer nam) {
        this.nam = nam;
    }

    public BigDecimal getTienTangCa() {
        return tienTangCa;
    }

    public void setTienTangCa(BigDecimal tienTangCa) {
        this.tienTangCa = tienTangCa;
    }

    public Integer getSoNguoiPhuThuoc() {
        return soNguoiPhuThuoc;
    }

    public void setSoNguoiPhuThuoc(Integer soNguoiPhuThuoc) {
        this.soNguoiPhuThuoc = soNguoiPhuThuoc;
    }

    public BigDecimal getThueThuNhap() {
        return thueThuNhap;
    }

    public void setThueThuNhap(BigDecimal thueThuNhap) {
        this.thueThuNhap = thueThuNhap;
    }

    public BigDecimal getTongThuNhap() {
        return tongThuNhap;
    }

    public void setTongThuNhap(BigDecimal tongThuNhap) {
        this.tongThuNhap = tongThuNhap;
    }

    public LocalDate getNgayNhan() {
        return ngayNhan;
    }

    public void setNgayNhan(LocalDate ngayNhan) {
        this.ngayNhan = ngayNhan;
    }
}

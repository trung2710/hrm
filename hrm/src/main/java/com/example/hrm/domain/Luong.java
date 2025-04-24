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
    private NhanVien nv_id;
    @Column(name = "Thang")
    private Integer thang;
    @Column(name = "Nam")
    private Integer nam;
    @Column(name = "tien_tang_ca", columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal tienTangCa;
    @Column(name = "SoNguoiPhuThuoc", columnDefinition = "INT DEFAULT 0")
    private Integer soNguoiPhuThuoc = 0;

    @Column(name = "ThueThuNhap", nullable = false)
    private BigDecimal thueThuNhap;

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

    public NhanVien getNv_id() {
        return nv_id;
    }

    public void setNv_id(NhanVien nv_id) {
        this.nv_id = nv_id;
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

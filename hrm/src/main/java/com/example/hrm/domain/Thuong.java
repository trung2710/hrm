package com.example.hrm.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="Thuong")
public class Thuong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThuong")
    private long id;
    @Column(name = "TenThuong", nullable = false)
    private String tenThuong;
    @Column(name = "MucThuong", nullable = false)
    private BigDecimal mucThuong;
    @OneToOne
    @JoinColumn(name="NguoiRaQuyetDinh")
    private NhanVien nguoiRaQuyetDinh;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTenThuong() {
        return tenThuong;
    }

    public void setTenThuong(String tenThuong) {
        this.tenThuong = tenThuong;
    }

    public BigDecimal getMucThuong() {
        return mucThuong;
    }

    public void setMucThuong(BigDecimal mucThuong) {
        this.mucThuong = mucThuong;
    }

    public NhanVien getNguoiRaQuyetDinh() {
        return nguoiRaQuyetDinh;
    }

    public void setNguoiRaQuyetDinh(NhanVien nguoiRaQuyetDinh) {
        this.nguoiRaQuyetDinh = nguoiRaQuyetDinh;
    }
}

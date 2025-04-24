package com.example.hrm.domain;


import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "PhongBan",schema = "dbo")
public class PhongBan {
    public PhongBan() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhongBan")
    private Integer id;

    @Column(name = "TenPhongBan", nullable = false)
    private String tenPhongBan;

    @Transient
    private String tenTruongPhong;
    @Transient
    private Integer soNhanVien;

    @OneToMany(mappedBy = "phongBan")
    private List<NhanVien> nhanViens;

    public List<NhanVien> getNhanViens() {
        return nhanViens;
    }

    public void setNhanViens(List<NhanVien> nhanViens) {
        this.nhanViens = nhanViens;
    }

    public String getTenTruongPhong() {
        return tenTruongPhong;
    }

    public void setTenTruongPhong(String tenTruongPhong) {
        this.tenTruongPhong = tenTruongPhong;
    }

    public Integer getSoNhanVien() {
        return soNhanVien;
    }

    public void setSoNhanVien(Integer soNhanVien) {
        this.soNhanVien = soNhanVien;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenPhongBan() {
        return tenPhongBan;
    }

    public void setTenPhongBan(String tenPhongBan) {
        this.tenPhongBan = tenPhongBan;
    }
}

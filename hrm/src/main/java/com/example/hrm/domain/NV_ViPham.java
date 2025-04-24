package com.example.hrm.domain;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name="NhanVien_ViPham")
public class NV_ViPham {
    @Id
    @ManyToOne
    @JoinColumn(name = "MaNhanVien", referencedColumnName = "MaNhanVien")
    private NhanVien nhanVien;

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "MaViPham", referencedColumnName = "MaViPham")
    private ViPham vp_id;

    @Column(name = "NgayViPham", nullable = false)
    private LocalDate ngayViPham;

    @Column(name = "MoTa")
    private String moTa;

    @OneToOne
    @JoinColumn(name="NguoiRaQuyetDinh", referencedColumnName="MaNhanVien")
    private NhanVien NguoiRaQuyetDinh;

    @Column(name = "NgayRaQuyetDinh", nullable = false)
    private LocalDate ngayRaQuyetDinh;



    public ViPham getVp_id() {
        return vp_id;
    }

    public void setVp_id(ViPham vp_id) {
        this.vp_id = vp_id;
    }

    public LocalDate getNgayViPham() {
        return ngayViPham;
    }

    public void setNgayViPham(LocalDate ngayViPham) {
        this.ngayViPham = ngayViPham;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public NhanVien getNguoiRaQuyetDinh() {
        return NguoiRaQuyetDinh;
    }

    public void setNguoiRaQuyetDinh(NhanVien nguoiRaQuyetDinh) {
        NguoiRaQuyetDinh = nguoiRaQuyetDinh;
    }

    public LocalDate getNgayRaQuyetDinh() {
        return ngayRaQuyetDinh;
    }

    public void setNgayRaQuyetDinh(LocalDate ngayRaQuyetDinh) {
        this.ngayRaQuyetDinh = ngayRaQuyetDinh;
    }
}

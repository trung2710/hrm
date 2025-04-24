package com.example.hrm.domain;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name="DonTu")
public class DonTu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonTu")
    private long id;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @Column(name = "LoaiDon", nullable = false)
    private String loaiDon;

    @Column(name = "NgayGuiDon")
    private LocalDate ngayGuiDon = LocalDate.now();

    @Column(name = "TrangThai")
    private String trangThai = "Chờ duyệt";

    @ManyToOne
    @JoinColumn(name = "NguoiPheDuyet")
    private NhanVien nguoiPheDuyet;

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

    public String getLoaiDon() {
        return loaiDon;
    }

    public void setLoaiDon(String loaiDon) {
        this.loaiDon = loaiDon;
    }

    public LocalDate getNgayGuiDon() {
        return ngayGuiDon;
    }

    public void setNgayGuiDon(LocalDate ngayGuiDon) {
        this.ngayGuiDon = ngayGuiDon;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public NhanVien getNguoiPheDuyet() {
        return nguoiPheDuyet;
    }

    public void setNguoiPheDuyet(NhanVien nguoiPheDuyet) {
        this.nguoiPheDuyet = nguoiPheDuyet;
    }
}

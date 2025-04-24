package com.example.hrm.domain;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name="NhanVien_DuAn")
public class NV_DuAn {
    @Id
    @ManyToOne
    @JoinColumn(name = "MaNhanVien", referencedColumnName = "MaNhanVien")
    private NhanVien nhanVien;
    
    @Id
    @ManyToOne
    @JoinColumn(name = "MaDuAn", referencedColumnName = "MaDuAn")
    private DuAn duAn;
    @Column(name = "VaiTro")
    private String vaiTro;
    @Column(name = "NgayThamGia")
    private LocalDate ngayThamGia;

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public DuAn getDuAn() {
        return duAn;
    }

    public void setDuAn(DuAn duAn) {
        this.duAn = duAn;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public LocalDate getNgayThamGia() {
        return ngayThamGia;
    }

    public void setNgayThamGia(LocalDate ngayThamGia) {
        this.ngayThamGia = ngayThamGia;
    }
}

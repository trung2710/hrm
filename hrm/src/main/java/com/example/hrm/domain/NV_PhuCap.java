package com.example.hrm.domain;

import com.example.hrm.domain.idClass.NVPhuCapId;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@IdClass(NVPhuCapId.class)
@Table(name="NV_Phucap")
public class NV_PhuCap {
    @Id
    @ManyToOne
    @JoinColumn(name = "MaNhanVien", referencedColumnName = "MaNhanVien")
    private NhanVien nhanVien;
    
    @Id 
    @ManyToOne
    @JoinColumn(name = "MaPhuCap", referencedColumnName = "MaPhuCap")
    private PhuCap phuCap;

    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;
    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;
    @Column(name = "MucTien", nullable = false)
    private BigDecimal mucTien;

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

    public PhuCap getPhuCap() {
        return phuCap;
    }

    public void setPhuCap(PhuCap phuCap) {
        this.phuCap = phuCap;
    }
}

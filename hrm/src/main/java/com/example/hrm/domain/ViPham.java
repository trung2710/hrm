package com.example.hrm.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity

@Table(name="ViPham")
public class ViPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaViPham")
    private long id;
    //phat tien, den bu, canh cao, nhac nho, sa thai
    @Column(name = "LoaiViPham", nullable = false)
    private String loaiViPham;

    @Column(name = "HinhThucPhat", nullable = false)
    private String hinhThucPhat;

    @Column(name = "SoTienPhat")
    private BigDecimal soTienPhat;


    public long getId() {
        return id;
    }

    public String getLoaiViPham() {
        return loaiViPham;
    }

    public void setLoaiViPham(String loaiViPham) {
        this.loaiViPham = loaiViPham;
    }

    public String getHinhThucPhat() {
        return hinhThucPhat;
    }

    public void setHinhThucPhat(String hinhThucPhat) {
        this.hinhThucPhat = hinhThucPhat;
    }

    public BigDecimal getSoTienPhat() {
        return soTienPhat;
    }

    public void setSoTienPhat(BigDecimal soTienPhat) {
        this.soTienPhat = soTienPhat;
    }

    public void setId(long id) {
        this.id = id;
    }
}

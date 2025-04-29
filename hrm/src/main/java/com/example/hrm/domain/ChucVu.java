package com.example.hrm.domain;


import jakarta.persistence.*;


@Entity
@Table(name = "ChucVu")

public class ChucVu {
    public ChucVu() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChucVu")
    private Integer id;
    @Column(name = "TenChucVu", nullable = false)
    private String tenChucVu;

    @OneToOne
    @JoinColumn(name = "MaQuyen", referencedColumnName = "MaQuyen")
    private Quyen quyen;

    @ManyToOne
    @JoinColumn(name = "MaPhongBan", referencedColumnName = "MaPhongBan")
    private PhongBan phongBan;

    public PhongBan getPhongBan() {
        return phongBan;
    }

    public void setPhongBan(PhongBan phongBan) {
        this.phongBan = phongBan;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenChucVu() {
        return tenChucVu;
    }

    public void setTenChucVu(String tenChucVu) {
        this.tenChucVu = tenChucVu;
    }

    public Quyen getQuyen() {
        return quyen;
    }

    public void setQuyen(Quyen quyen) {
        this.quyen = quyen;
    }

}

package com.example.hrm.domain;


import jakarta.persistence.*;

@Entity
@Table(name = "PhongBan")
public class PhongBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhongBan")
    private Integer id;

    @Column(name = "TenPhongBan", nullable = false)
    private String tenPhongBan;

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

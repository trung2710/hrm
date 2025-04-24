package com.example.hrm.domain;


import jakarta.persistence.*;

@Entity
@Table(name = "Quyen")
public class Quyen {
    public Quyen() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaQuyen")
    private Integer id;

    @Column(name = "TenQuyen", nullable = false, unique = true)
    private String tenQuyen;

    @Column(name = "MoTa")
    private String moTa;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenQuyen() {
        return tenQuyen;
    }

    public void setTenQuyen(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}

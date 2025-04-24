package com.example.hrm.domain;


import jakarta.persistence.*;


@Entity
@Table(name = "ChucVu")

public class ChucVu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChucVu")
    private Integer id;

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

    @Column(name = "TenChucVu", nullable = false)
    private String tenChucVu;

    @OneToOne
    @JoinColumn(name = "MaQuyen", referencedColumnName = "MaQuyen")
    private Quyen quyen;
}

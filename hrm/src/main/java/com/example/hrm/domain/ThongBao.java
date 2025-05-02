package com.example.hrm.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name="ThongBao")
public class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThongBao")
    private long id;

    @Column(name = "TieuDe", nullable = false)
    private String tieuDe;

    @Column(name = "NoiDung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "ThoiGianTao")
    private LocalDateTime thoiGianTao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "NguoiTao", nullable = false)
    private NhanVien nguoiTao;

    @Column(name = "PhamVi", nullable = false)
    private String phamVi;

    @Column(name = "DoiTuong")
    private String doiTuong;

    @Transient
    private List<String> listDoiTuong;

    public List<String> getListDoiTuong() {
        return listDoiTuong;
    }

    public void setListDoiTuong(List<String> listDoiTuong) {
        this.listDoiTuong = listDoiTuong;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public NhanVien getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(NhanVien nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public String getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(String phamVi) {
        this.phamVi = phamVi;
    }

    public String getDoiTuong() {
        return doiTuong;
    }

    public void setDoiTuong(String doiTuong) {
        this.doiTuong = doiTuong;
    }
}

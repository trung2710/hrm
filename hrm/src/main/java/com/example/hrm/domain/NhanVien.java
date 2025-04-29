package com.example.hrm.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="NhanVien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNhanVien")
    private Integer id;

    @NotNull
    @Size(min=3, message="ho ten phai co toi thieu 3 ki tu")
    @Column(name = "HoTen", nullable = false)
    private String hoTen;
    @Column(name = "GioiTinh")
    private String gioiTinh;
    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;
    @Column(name = "CCCD", unique = true)
    private String cccd;
    @Column(name = "TrinhDoHocVan")
    private String hocVan;
    @Column(name = "DiaChi")
    private String diaChi;
    @Column(name = "SoNgayPhep")
    private Integer soNgayPhep=12;
    @Column(name = "ThamNien")
    private Integer thamNien;
    @Column(name = "TrangThaiLamViec")
    private String trangThai;
    
    @NotNull
    @Email(message = "Email is not valid", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @Column(name = "Email", unique = true)
    private String email;
    
    @NotNull
    @Size(min=7,message="Vui long nhap day du so dien thoai")
    @Column(name = "SDT")
    private String soDienThoai;

    @NotNull
    @Size(min=3, message="Password phai co toi thieu 3 ki tu")
    @Column(name = "MatKhau")
    private String password;
    @Column(name = "LuongHienTai", nullable = false)
    private BigDecimal luongHienTai;

    @Column(name = "Avatar")
    private String avatar;


    @ManyToOne
    @JoinColumn(name = "MaChucVu", referencedColumnName = "MaChucVu")
    private ChucVu chucVu;

    
     @OneToMany(mappedBy = "nhanVien")
     List<HopDong> hopDongs;

     @OneToMany(mappedBy="nhanVien")
     List<NV_ViPham>viPhams;

     @OneToMany(mappedBy="nhanVien")
     List<NghiPhep> nghiPheps;

     @OneToMany(mappedBy="nhanVien")
     List<NV_DuAn> duAns;

     @OneToMany(mappedBy="nhanVien")
     List<BaoHiem> baoHiems;

     @OneToMany(mappedBy="nhanVien")
     List<DonTu> donTus;

     @OneToMany(mappedBy="nhanVien")
     List<NV_Thuong> thuongs;

     @OneToMany(mappedBy="nhanVien")
     List<NV_PhuCap> phuCaps;

     @OneToMany(mappedBy="nguoiTao")
     List<ThongBao> thongBaos;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getHocVan() {
        return hocVan;
    }

    public void setHocVan(String hocVan) {
        this.hocVan = hocVan;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public Integer getSoNgayPhep() {
        return soNgayPhep;
    }

    public void setSoNgayPhep(Integer soNgayPhep) {
        this.soNgayPhep = soNgayPhep;
    }

    public Integer getThamNien() {
        return thamNien;
    }

    public void setThamNien(Integer thamNien) {
        this.thamNien = thamNien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getLuongHienTai() {
        return luongHienTai;
    }

    public void setLuongHienTai(BigDecimal luongHienTai) {
        this.luongHienTai = luongHienTai;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ChucVu getChucVu() {
        return chucVu;
    }

    public void setChucVu(ChucVu chucVu) {
        this.chucVu = chucVu;
    }


    public List<HopDong> getHopDongs() {
        return hopDongs;
    }

    public void setHopDongs(List<HopDong> hopDongs) {
        this.hopDongs = hopDongs;
    }

    public List<NV_ViPham> getViPhams() {
        return viPhams;
    }

    public void setViPhams(List<NV_ViPham> viPhams) {
        this.viPhams = viPhams;
    }

    public List<NghiPhep> getNghiPheps() {
        return nghiPheps;
    }

    public void setNghiPheps(List<NghiPhep> nghiPheps) {
        this.nghiPheps = nghiPheps;
    }

    public List<NV_DuAn> getDuAns() {
        return duAns;
    }

    public void setDuAns(List<NV_DuAn> duAns) {
        this.duAns = duAns;
    }

    public List<BaoHiem> getBaoHiems() {
        return baoHiems;
    }

    public void setBaoHiems(List<BaoHiem> baoHiems) {
        this.baoHiems = baoHiems;
    }

    public List<DonTu> getDonTus() {
        return donTus;
    }

    public void setDonTus(List<DonTu> donTus) {
        this.donTus = donTus;
    }

    public List<NV_Thuong> getThuongs() {
        return thuongs;
    }

    public void setThuongs(List<NV_Thuong> thuongs) {
        this.thuongs = thuongs;
    }

    public List<NV_PhuCap> getPhuCaps() {
        return phuCaps;
    }

    public void setPhuCaps(List<NV_PhuCap> phuCaps) {
        this.phuCaps = phuCaps;
    }

    public List<ThongBao> getThongBaos() {
        return thongBaos;
    }

    public void setThongBaos(List<ThongBao> thongBaos) {
        this.thongBaos = thongBaos;
    }
}

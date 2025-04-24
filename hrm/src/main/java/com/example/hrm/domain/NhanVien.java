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
    private String HoTen;
    @Column(name = "GioiTinh")
    private String GioiTinh;
    @Column(name = "NgaySinh")
    private LocalDate NgaySinh;
    @Column(name = "CCCD", unique = true)
    private String CCCD;
    @Column(name = "TrinhDoHocVan")
    private String HocVan;
    @Column(name = "DiaChi")
    private String DiaChi;
    @Column(name = "SoNgayPhep")
    private Integer SoNgayPhep=12;
    @Column(name = "ThamNien")
    private Integer ThamNien;
    @Column(name = "TrangThaiLamViec")
    private String TrangThai;
    
    @NotNull
    @Email(message = "Email is not valid", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @Column(name = "Email", unique = true)
    private String Email;
    
    @NotNull
    @Size(min=7,message="Vui long nhap day du so dien thoai")
    @Column(name = "SDT")
    private String SoDienThoai;

    @NotNull
    @Size(min=3, message="Password phai co toi thieu 3 ki tu")
    @Column(name = "MatKhau")
    private String Password;
    @Column(name = "LuongHienTai", nullable = false)
    private BigDecimal luongHienTai;

    @Column(name = "Avatar")
    private String avatar;


    @ManyToOne
    @JoinColumn(name = "MaChucVu", referencedColumnName = "MaChucVu")
    private ChucVu chucVu;

    @ManyToOne
    @JoinColumn(name = "MaPhongBan", referencedColumnName = "MaPhongBan")
    private PhongBan phongBan;
    
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
        return HoTen;
    }

    public void setHoTen(String hoTen) {
        HoTen = hoTen;
    }

    public String getGioiTinh() {
        return GioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        GioiTinh = gioiTinh;
    }

    public LocalDate getNgaySinh() {
        return NgaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        NgaySinh = ngaySinh;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public String getHocVan() {
        return HocVan;
    }

    public void setHocVan(String hocVan) {
        HocVan = hocVan;
    }

    public String getDiaChi() {
        return DiaChi;
    }

    public void setDiaChi(String diaChi) {
        DiaChi = diaChi;
    }

    public Integer getSoNgayPhep() {
        return SoNgayPhep;
    }

    public void setSoNgayPhep(Integer soNgayPhep) {
        SoNgayPhep = soNgayPhep;
    }

    public Integer getThamNien() {
        return ThamNien;
    }

    public void setThamNien(Integer thamNien) {
        ThamNien = thamNien;
    }

    public String getTrangThai() {
        return TrangThai;
    }

    public void setTrangThai(String trangThai) {
        TrangThai = trangThai;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getSoDienThoai() {
        return SoDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        SoDienThoai = soDienThoai;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
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

    public PhongBan getPhongBan() {
        return phongBan;
    }

    public void setPhongBan(PhongBan phongBan) {
        this.phongBan = phongBan;
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

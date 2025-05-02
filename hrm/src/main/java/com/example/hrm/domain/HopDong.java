 package com.example.hrm.domain;

 import java.math.BigDecimal;
 import java.time.LocalDate;

 import jakarta.persistence.*;

 @Entity
 @Table(name="HopDong")
 public class HopDong {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "MaHopDong")
     private long id;
    
     @ManyToOne
     @JoinColumn(name = "MaNhanVien", referencedColumnName = "MaNhanVien")
     private NhanVien nhanVien;

     @Column(name = "LoaiHopDong", nullable = false)
     private String loaiHopDong;


     @Column(name = "NgayBatDau", nullable = false)
     private LocalDate ngayBatDau;

     @Column(name = "NgayKetThuc", nullable = false)
     private LocalDate ngayKetThuc;

     public long getId() {
         return id;
     }

     public void setId(long id) {
         this.id = id;
     }

     public NhanVien getNhanVien() {
         return nhanVien;
     }

     public void setNhanVien(NhanVien nhanVien) {
         this.nhanVien = nhanVien;
     }

     public String getLoaiHopDong() {
         return loaiHopDong;
     }

     public void setLoaiHopDong(String loaiHopDong) {
         this.loaiHopDong = loaiHopDong;
     }

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

     public BigDecimal getLuongCoBan() {
         return luongCoBan;
     }

     public void setLuongCoBan(BigDecimal luongCoBan) {
         this.luongCoBan = luongCoBan;
     }

     public String getTrangThai() {
         return trangThai;
     }

     public void setTrangThai(String trangThai) {
         this.trangThai = trangThai;
     }

     @Column(name = "LuongCoBan", nullable = false)
     private BigDecimal luongCoBan;

     @Column(name = "TrangThai", nullable = false)
     private String trangThai = "Còn hiệu lực";  // Mặc định trạng thái là "Còn hiệu lực"
    
 }

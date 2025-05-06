package com.example.hrm.domain.idClass;

import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.ViPham;

import java.io.Serializable;
import java.util.Objects;

public class NVViPhamId implements Serializable {
    ViPham viPham;
    NhanVien nhanVien;
    public NVViPhamId() {}
    public NVViPhamId(ViPham viPham, NhanVien nhanVien) {
        this.viPham = viPham;
        this.nhanVien = nhanVien;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NVViPhamId that = (NVViPhamId) o;
        return Objects.equals(viPham, that.viPham) && Objects.equals(nhanVien, that.nhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(viPham, nhanVien);
    }

    public ViPham getViPham() {
        return viPham;
    }

    public void setViPham(ViPham viPham) {
        this.viPham = viPham;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }
}

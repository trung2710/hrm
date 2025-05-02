package com.example.hrm.domain.idClass;


import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.Thuong;

import java.io.Serializable;
import java.util.Objects;

public class NVThuongId implements Serializable {
    Thuong thuong;
    NhanVien nhanVien;
    public NVThuongId() {}
    public NVThuongId(Thuong thuong, NhanVien nhanVien) {
        this.thuong = thuong;
        this.nhanVien = nhanVien;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NVThuongId that = (NVThuongId) o;
        return Objects.equals(thuong, that.thuong) && Objects.equals(nhanVien, that.nhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thuong, nhanVien);
    }
}

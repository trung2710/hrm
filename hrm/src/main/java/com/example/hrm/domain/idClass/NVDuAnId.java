package com.example.hrm.domain.idClass;

import com.example.hrm.domain.DuAn;
import com.example.hrm.domain.NhanVien;

import java.io.Serializable;
import java.util.Objects;

public class NVDuAnId implements Serializable {
    DuAn duAn;
    NhanVien nhanVien;
    public NVDuAnId() {}
    public NVDuAnId(DuAn duAn, NhanVien nhanVien) {
        this.duAn = duAn;
        this.nhanVien = nhanVien;
    }

    public DuAn getDuAn() {
        return duAn;
    }

    public void setDuAn(DuAn duAn) {
        this.duAn = duAn;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NVDuAnId nvDuAnId = (NVDuAnId) o;
        return Objects.equals(duAn, nvDuAnId.duAn) && Objects.equals(nhanVien, nvDuAnId.nhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duAn, nhanVien);
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }
}

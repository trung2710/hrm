package com.example.hrm.domain.idClass;

import com.example.hrm.domain.NhanVien;
import com.example.hrm.domain.PhuCap;

import java.io.Serializable;
import java.util.Objects;

public class NVPhuCapId implements Serializable {
    private NhanVien nhanVien;
    private PhuCap phuCap;

    public NVPhuCapId() {}

    public NVPhuCapId(NhanVien nhanVien, PhuCap phuCap) {
        this.nhanVien = nhanVien;
        this.phuCap = phuCap;
    }

    // equals & hashCode bắt buộc
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NVPhuCapId)) return false;
        NVPhuCapId that = (NVPhuCapId) o;
        return Objects.equals(nhanVien, that.nhanVien) &&
                Objects.equals(phuCap, that.phuCap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nhanVien, phuCap);
    }
}

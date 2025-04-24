package com.example.hrm.constant;

public enum DepartmentEnum {
    NHAN_SU("Phòng Nhân sự"),
    KE_TOAN("Phòng Tài chính - Kế toán"),
    IT("Phòng Công nghệ thông tin"),
    KINH_DOANH("Phòng Kinh doanh"),
    MARKETING("Phòng Marketing"),
    HANH_CHINH("Phòng Hành chính"),
    KY_THUAT("Phòng Kỹ thuật"),
    NGHIEN_CUU_PHAT_TRIEN("Phòng Nghiên cứu và Phát triển"),
    BAN_GIAM_DOC("Ban Giám đốc"),
    CHAM_SOC_KHACH_HANG("Phòng Chăm sóc khách hàng");

    private final String tenTiengViet;

    DepartmentEnum(String tenTiengViet) {
        this.tenTiengViet = tenTiengViet;
    }

    public String getTenTiengViet() {
        return tenTiengViet;
    }

    @Override
    public String toString() {
        return tenTiengViet;
    }
}

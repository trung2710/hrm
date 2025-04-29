package com.example.hrm.constant;

public enum NhanVienStatusEnum {
    ACTIVE("Đang làm"),
    RETIRE("Nghỉ việc"),
    INACTIVE("Đang nghỉ phép");

    private String value;

    NhanVienStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

//    @Override
//    public String toString() {
//        return value;
//    }



}

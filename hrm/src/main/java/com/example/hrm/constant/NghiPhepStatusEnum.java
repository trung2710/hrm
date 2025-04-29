package com.example.hrm.constant;

public enum NghiPhepStatusEnum {
    WAITING("Chờ duyệt"),
    REFUSE("Từ chối"),
    APPROVED("Đã duyệt");

    private String value;
    NghiPhepStatusEnum(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    @Override
    public String toString() {
        return value;
    }
}

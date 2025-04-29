package com.example.hrm.constant;

public enum DonTuStatusEnum {
    WAITING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REFUSE("Từ chối");

    private String value;
    DonTuStatusEnum(String value) {
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

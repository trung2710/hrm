package com.example.hrm.constant;

public enum DuAnStatusEnum {
    DEPLOY("Đang triển khai"),
    COMPLETE("Hoàn thành"),
    PAUSE("Tạm dừng");

    private String value;
    DuAnStatusEnum(String value) {
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

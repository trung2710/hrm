package com.example.hrm.constant;

public enum ChamCongStatusEnum {
    ONTIME("Đúng giờ"),
    LATE("Muộn"),
    ABSENT("Vắng");

    private String value;
    ChamCongStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

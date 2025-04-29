package com.example.hrm.constant;

public enum PhuCapTypeEnum {
    CODINH("Cố định"),
    LIMIT("Có thời hạn"),
    Luong("Theo phần trăm lương");
    private String value;
    PhuCapTypeEnum(String value) {
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

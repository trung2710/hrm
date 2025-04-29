package com.example.hrm.constant;

public enum ContractStatusEnum {
    VALID("Còn hiệu lực"),
    EXPIRED("Hết hạn"),
    TERMINATE("Chấm dứt");
    private String value;
    ContractStatusEnum(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}

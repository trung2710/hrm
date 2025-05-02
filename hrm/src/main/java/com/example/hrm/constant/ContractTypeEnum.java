package com.example.hrm.constant;

public enum ContractTypeEnum {
    FULLTIME("Fulltime"),
    PARTTIME("Parttime"),
    THUVIEC("Probation");
    private String value;
    ContractTypeEnum(String value) {
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

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
    public String getBadgeClass() {
        switch(this) {
            case WAITING: return "bg-warning";
            case APPROVED: return "bg-success";
            case REFUSE: return "bg-danger";
            default: return "bg-secondary";
        }
    }
}

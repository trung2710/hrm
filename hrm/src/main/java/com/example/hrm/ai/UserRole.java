package com.example.hrm.ai;

public enum UserRole {
    EMPLOYEE("Nhân viên"),
    MANAGER("Quản lý"),
    HR("Nhân sự"),
    ADMIN("Quản trị viên"),
    GUEST("Khách");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

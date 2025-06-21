package com.example.hrm.ai;

public enum QueryType {
    // Employee queries
    MY_INFO,
    MY_SALARY,
    MY_ATTENDANCE,
    MY_LEAVE,
    MY_CONTRACT,
    MY_ALLOWANCE,
    MY_BONUS,
    MY_VIOLATION,
    MY_PROJECT,
    MY_INSURANCE,

    TEAM_INFO,
    // ✅ NEW: Employee search queries
    EMPLOYEE_SEARCH,            // Tìm kiếm thông tin nhân viên cụ thể
    // ✅ NEW: Admin contract queries
    ADMIN_CONTRACT_INFO,        // Xem hợp đồng của nhân viên khác
    // ✅ NEW: Admin management queries
    ADMIN_ALL_EMPLOYEES,        // Danh sách tất cả nhân viên
    ADMIN_DEPARTMENT_REPORT,    // Báo cáo theo phòng ban
    ADMIN_SALARY_ANALYSIS,      // Top lương, phân tích lương
    ADMIN_ATTENDANCE_REPORT,    // Báo cáo chấm công, đi muộn
    ADMIN_COMPANY_STATS,        // Thống kê tổng quan công ty
    ADMIN_FINANCIAL_ANALYSIS,   // Phân tích chi phí, tổng chi phí
    ADMIN_CONTRACT_REPORT,
    // Admin queries
    SYSTEM_STATS,
    USER_MANAGEMENT,
    FULL_REPORT,

    // Special
    AI_GENERATED,
    UNKNOWN,
    PERMISSION_DENIED
}

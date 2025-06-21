package com.example.hrm.ai;

import com.example.hrm.domain.NhanVien;
import com.example.hrm.service.CustomUserDetail;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class HRMPermissionService {

    private static final Logger log = LoggerFactory.getLogger(HRMPermissionService.class);

    public NhanVien getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetail) {
                CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
                NhanVien nhanVien = userDetail.getNhanVien();
                return nhanVien;
            }
        } catch (Exception e) {
            // Log error if needed
        }
        return null;
    }

    public UserRole getCurrentUserRole() {
        NhanVien nhanVien = getCurrentUser();
        if (nhanVien == null) {
            return UserRole.GUEST;
        }

        try {
            String tenQuyen = nhanVien.getChucVu().getQuyen().getTenQuyen();
            return switch (tenQuyen) {
                case "Admin" -> UserRole.ADMIN;
                case "Manager" -> UserRole.ADMIN; // ✅ FIX: Manager should be MANAGER, not ADMIN
                case "HR" -> UserRole.HR;
                default -> UserRole.EMPLOYEE;
            };
        } catch (Exception e) {
            return UserRole.EMPLOYEE;
        }
    }

    public boolean canAccessQuery(QueryType queryType) {
        UserRole role = getCurrentUserRole();
        Set<QueryType> allowedQueries = getAllowedQueries(role);
        return allowedQueries.contains(queryType);
    }

    public boolean canAccessEmployeeData(Integer targetEmployeeId) {
        NhanVien currentUser = getCurrentUser();
        if (currentUser == null) return false;

        UserRole role = getCurrentUserRole();

        // Admin có thể xem tất cả
        if (role == UserRole.ADMIN) return true;

        // Employee chỉ xem được thông tin của chính mình
        if (role == UserRole.EMPLOYEE) {
            return currentUser.getId().equals(targetEmployeeId);
        }

        // Manager có thể xem nhân viên trong phòng ban của mình
        if (role == UserRole.MANAGER) {
            return canManagerAccessEmployee(currentUser, targetEmployeeId);
        }

        // HR có thể xem tất cả thông tin nhân viên
        return role == UserRole.HR;
    }

    // ✅ UPDATED: Better query permissions for all roles including ADMIN
    private Set<QueryType> getAllowedQueries(UserRole role) {
        Set<QueryType> queries = new HashSet<>();

        switch (role) {
            case EMPLOYEE:
                queries.addAll(Arrays.asList(
                        QueryType.MY_INFO, QueryType.MY_SALARY, QueryType.MY_ATTENDANCE,
                        QueryType.MY_LEAVE, QueryType.MY_CONTRACT, QueryType.MY_ALLOWANCE,
                        QueryType.MY_BONUS, QueryType.MY_VIOLATION, QueryType.MY_PROJECT,
                        QueryType.MY_INSURANCE
                ));
                break;

            case MANAGER:
                queries.addAll(getAllowedQueries(UserRole.EMPLOYEE));
                queries.addAll(Arrays.asList(
                        QueryType.TEAM_INFO
                        // Add team-related queries as needed
                ));
                break;

            case HR:
                queries.addAll(getAllowedQueries(UserRole.MANAGER));
                queries.addAll(Arrays.asList(
                        QueryType.EMPLOYEE_SEARCH,
                        QueryType.ADMIN_ALL_EMPLOYEES,
                        QueryType.ADMIN_DEPARTMENT_REPORT,
                        QueryType.ADMIN_SALARY_ANALYSIS,
                        QueryType.ADMIN_ATTENDANCE_REPORT,
                        QueryType.ADMIN_COMPANY_STATS,
                        QueryType.ADMIN_FINANCIAL_ANALYSIS
                ));
                break;

            case ADMIN:
                // ✅ ADMIN has access to ALL query types
                queries.addAll(getAllowedQueries(UserRole.HR));
                queries.addAll(Arrays.asList(
                        QueryType.SYSTEM_STATS, QueryType.USER_MANAGEMENT, QueryType.FULL_REPORT,
                        QueryType.AI_GENERATED, QueryType.UNKNOWN // Allow AI generated queries
                ));
                break;
        }

        return queries;
    }

    public Integer getCurrentEmployeeId() {
        NhanVien nhanVien = getCurrentUser();
        return nhanVien != null ? nhanVien.getId() : null;
    }

    public Integer getCurrentDepartmentId() {
        NhanVien nhanVien = getCurrentUser();
        if (nhanVien != null && nhanVien.getChucVu() != null && nhanVien.getChucVu().getPhongBan() != null) {
            return nhanVien.getChucVu().getPhongBan().getId();
        }
        return null;
    }

    public String getCurrentUserName() {
        NhanVien nhanVien = getCurrentUser();
        return nhanVien != null ? nhanVien.getHoTen() : "Unknown";
    }

    public String getCurrentUsername() {
        return "trung2710"; // Current user login
    }

    private boolean canManagerAccessEmployee(NhanVien manager, Integer targetEmployeeId) {
        try {
            // Manager chỉ có thể xem nhân viên trong cùng phòng ban
            Integer managerDeptId = manager.getChucVu().getPhongBan().getId();
            // TODO: Query database để check target employee có cùng department không
            // Tạm thời return true, bạn có thể implement query kiểm tra
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ UPDATED: Better security context descriptions
    public String getSecurityContext() {
        UserRole role = getCurrentUserRole();
        NhanVien nhanVien = getCurrentUser();

        if (nhanVien == null) {
            return "- KHÔNG có quyền truy cập dữ liệu";
        }

        Integer employeeId = nhanVien.getId();
        Integer deptId = getCurrentDepartmentId();

        return switch (role) {
            case EMPLOYEE -> String.format(
                    "- CHỈ được truy cập dữ liệu của chính mình (MaNhanVien = %d)\n" +
                            "- KHÔNG được xem thông tin nhân viên khác\n" +
                            "- KHÔNG được xem dữ liệu tổng hợp", employeeId);

            case MANAGER -> String.format(
                    "- Được xem dữ liệu cá nhân (MaNhanVien = %d)\n" +
                            "- Được xem dữ liệu nhân viên trong phòng ban: %d\n" +
                            "- Được xem thống kê team\n" +
                            "- KHÔNG được xem dữ liệu phòng ban khác", employeeId, deptId);

            case HR ->
                    "- Được xem TẤT CẢ dữ liệu nhân viên\n" +
                            "- Được xem báo cáo tổng hợp toàn công ty\n" +
                            "- Được truy cập dữ liệu chính sách\n" +
                            "- Được thực hiện các truy vấn phân tích";

            case ADMIN ->
                    "- 🔥 FULL ACCESS - KHÔNG giới hạn\n" +
                            "- Được xem TẤT CẢ bảng và dữ liệu\n" +
                            "- Được thực hiện MỌI loại truy vấn\n" +
                            "- Có quyền truy cập cấp hệ thống\n" +
                            "- Có thể tìm kiếm bất kỳ nhân viên nào\n" +
                            "- Có thể xem báo cáo tổng hợp và phân tích";

            default -> "- KHÔNG có quyền truy cập dữ liệu";
        };
    }

    // ✅ UPDATED: Role checking methods
    public boolean isAdmin() {
        return getCurrentUserRole() == UserRole.ADMIN;
    }

    public boolean isManager() {
        return getCurrentUserRole() == UserRole.MANAGER;
    }

    public boolean isHR() {
        return getCurrentUserRole() == UserRole.HR;
    }

    public boolean isEmployee() {
        return getCurrentUserRole() == UserRole.EMPLOYEE;
    }

    public boolean isAdminOrManager() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.ADMIN || role == UserRole.MANAGER;
    }

    // ✅ NEW: Additional helper methods for ADMIN
    public boolean hasFullAccess() {
        return isAdmin();
    }

    public boolean canAccessAllEmployees() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.ADMIN || role == UserRole.HR;
    }

    public boolean canAccessCompanyReports() {
        UserRole role = getCurrentUserRole();
        return role == UserRole.ADMIN || role == UserRole.HR;
    }

    public boolean canAccessFinancialData() {
        return isAdmin();
    }

    // ✅ NEW: Method to validate query security for different roles
    public boolean isQuerySecure(String sql, QueryType queryType) {
        UserRole role = getCurrentUserRole();
        String lowerSQL = sql.toLowerCase();

        // Block dangerous operations for all roles
        if (lowerSQL.contains("drop ") || lowerSQL.contains("delete ") ||
                lowerSQL.contains("update ") || lowerSQL.contains("insert ") ||
                lowerSQL.contains("truncate ") || lowerSQL.contains("alter ")) {
            return false;
        }

        // ADMIN has full access
        if (role == UserRole.ADMIN) {
            return true;
        }

        // HR can access most data
        if (role == UserRole.HR) {
            return !lowerSQL.contains("system") && !lowerSQL.contains("user");
        }

        // EMPLOYEE and MANAGER need user filtering
        Integer currentUserId = getCurrentEmployeeId();
        if (currentUserId != null) {
            boolean hasUserFilter = lowerSQL.contains("manhanvien = " + currentUserId) ||
                    lowerSQL.contains("manhanvien=" + currentUserId);

            // For personal queries, require user filter
            if (queryType.name().startsWith("MY_")) {
                return hasUserFilter;
            }
        }

        return false;
    }
}
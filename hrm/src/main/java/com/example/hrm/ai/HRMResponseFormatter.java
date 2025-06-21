package com.example.hrm.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class HRMResponseFormatter {

    private static final Logger log = LoggerFactory.getLogger(HRMResponseFormatter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private HRMPermissionService permissionService;

    // ✅ SAFE PARSING METHODS - Fix all NumberFormatException
    private int safeParseInt(Object value) {
        if (value == null) return 0;
        try {
            if (value instanceof Number) return ((Number) value).intValue();
            String str = value.toString().trim();
            if ("N/A".equals(str) || str.isEmpty()) return 0;
            return Integer.parseInt(str);
        } catch (Exception e) {
            log.warn("⚠️ Cannot parse as int: '{}', returning 0", value);
            return 0;
        }
    }

    private double safeParseDouble(Object value) {
        if (value == null) return 0.0;
        try {
            if (value instanceof Number) return ((Number) value).doubleValue();
            String str = value.toString().trim();
            if ("N/A".equals(str) || str.isEmpty()) return 0.0;
            return Double.parseDouble(str);
        } catch (Exception e) {
            log.warn("⚠️ Cannot parse as double: '{}', returning 0.0", value);
            return 0.0;
        }
    }

    private long safeParseLong(Object value) {
        if (value == null) return 0L;
        try {
            if (value instanceof Number) return ((Number) value).longValue();
            String str = value.toString().trim();
            if ("N/A".equals(str) || str.isEmpty()) return 0L;
            return Long.parseLong(str);
        } catch (Exception e) {
            log.warn("⚠️ Cannot parse as long: '{}', returning 0", value);
            return 0L;
        }
    }

    private String safeGetString(Object value) {
        return (value != null) ? value.toString() : "N/A";
    }

    private String safeGetString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return (value != null) ? value.toString() : "N/A";
    }

    public String formatResponse(DatabaseResponse dbResponse) {
        if (!dbResponse.isSuccess()) {
            return "❌ " + dbResponse.getError();
        }

        List<Map<String, Object>> data = dbResponse.getData();
        if (data.isEmpty()) {
            return generateEmptyResultMessage(dbResponse.getQueryType());
        }

        String userName = permissionService.getCurrentUserName();

        return switch (dbResponse.getQueryType()) {
            // Personal queries
            case MY_INFO -> formatEmployeeInfo(data, userName);
            case MY_SALARY -> formatSalaryInfo(data, userName);
            case MY_ATTENDANCE -> formatAttendanceInfo(data, userName);
            case MY_LEAVE -> formatLeaveInfo(data, userName);
            case MY_CONTRACT -> formatContractInfo(data, userName);
            case MY_ALLOWANCE -> formatAllowanceInfo(data, userName);
            case MY_BONUS -> formatBonusInfo(data, userName);
            case MY_VIOLATION -> formatViolationInfo(data, userName);
            case TEAM_INFO -> formatTeamInfo(data);

            // Admin queries - SAFE VERSIONS
            case EMPLOYEE_SEARCH -> formatEmployeeSearchResult(data);
            case ADMIN_ALL_EMPLOYEES -> formatAllEmployeesReport(data);
            case ADMIN_DEPARTMENT_REPORT -> formatDepartmentReportSafe(data);
            case ADMIN_SALARY_ANALYSIS -> formatSalaryAnalysisSafe(data);
            case ADMIN_ATTENDANCE_REPORT -> formatAttendanceReportSafe(data);
            case ADMIN_COMPANY_STATS -> formatCompanyStatsSafe(data);
            case ADMIN_FINANCIAL_ANALYSIS -> formatFinancialAnalysisSafe(data);
            case ADMIN_CONTRACT_INFO -> formatAdminContractInfo(data);
            case ADMIN_CONTRACT_REPORT -> formatContractReportSafe(data, dbResponse);

            // AI Generated with smart detection
            case AI_GENERATED -> formatAIGeneratedResponseSmart(data, dbResponse);
            default -> formatGenericResponseSafe(data, dbResponse);
        };
    }

    // ✅ SAFE DEPARTMENT REPORT (Fix line 315 error)
    private String formatDepartmentReportSafe(List<Map<String, Object>> data) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🏢 **BÁO CÁO PHÒNG BAN**\n\n");

            BigDecimal grandTotal = BigDecimal.ZERO;
            int totalEmployees = 0;

            for (Map<String, Object> dept : data) {
                String tenPhongBan = safeGetString(dept.get("TenPhongBan"));

                // Handle different possible column names for overtime hours
                Object tongGioTangCa = dept.get("TongSoGioTangCa");
                if (tongGioTangCa == null) tongGioTangCa = dept.get("TongSoGioTangCa");
                if (tongGioTangCa == null) tongGioTangCa = dept.get("TongGioTangCa");

                // Safe parsing for various fields
                Object deptTotal = dept.get("TongLuong");
                Object deptAvg = dept.get("LuongTrungBinh");
                Object deptMin = dept.get("LuongThapNhat");
                Object deptMax = dept.get("LuongCaoNhat");
                Object empCount = dept.get("SoNhanVien");

                int employees = safeParseInt(empCount);
                totalEmployees += employees;

                BigDecimal deptTotalBD = getBigDecimal(deptTotal);
                grandTotal = grandTotal.add(deptTotalBD);

                if (tongGioTangCa != null) {
                    // Overtime report
                    sb.append(String.format("""
                        🏢 **%s**
                        ├─ ⏰ Tổng giờ tăng ca: %s giờ
                        ├─ 👥 Nhân viên: %d người
                        └─ 📊 TB tăng ca/người: %.1f giờ
                        
                        """,
                            tenPhongBan,
                            formatNumber(tongGioTangCa),
                            employees,
                            employees > 0 ? safeParseDouble(tongGioTangCa) / employees : 0.0
                    ));
                } else {
                    // Salary report
                    sb.append(String.format("""
                        🏢 **%s**
                        ├─ 👥 Số nhân viên: %d người
                        ├─ 💰 Lương trung bình: %s VNĐ
                        ├─ 📈 Lương cao nhất: %s VNĐ
                        ├─ 📉 Lương thấp nhất: %s VNĐ
                        ├─ 💵 Tổng lương phòng ban: %s VNĐ  
                        └─ 📊 %% tổng chi phí: %.1f%%
                        
                        """,
                            tenPhongBan,
                            employees,
                            formatCurrency(deptAvg),
                            formatCurrency(deptMax),
                            formatCurrency(deptMin),
                            formatCurrency(deptTotalBD),
                            grandTotal.compareTo(BigDecimal.ZERO) > 0 ?
                                    deptTotalBD.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 1, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0
                    ));
                }
            }

            sb.append(String.format("""
                📊 **TỔNG KẾT:**
                ├─ 🏢 Tổng số phòng ban: %d phòng
                ├─ 👥 Tổng nhân viên: %d người
                └─ 💰 Tổng chi phí: %s VNĐ
                """,
                    data.size(),
                    totalEmployees,
                    formatCurrency(grandTotal)
            ));

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ formatDepartmentReportSafe error: {}", e.getMessage());
            return formatGenericResponseSafe(data, null);
        }
    }
    // ✅ SAFE CONTRACT REPORT FORMATTER
    private String formatContractReportSafe(List<Map<String, Object>> data, DatabaseResponse dbResponse) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("📋 **DANH SÁCH HỢP ĐỒNG**\n\n");

            if (data.isEmpty()) {
                sb.append("📭 Không tìm thấy hợp đồng nào.\n");
                return sb.toString();
            }

            // Check if this is expiring contracts
            boolean isExpiringReport = data.stream()
                    .anyMatch(record -> record.containsKey("SoNgayConLai"));

            if (isExpiringReport) {
                sb.append("⏰ **HỢP ĐỒNG SẮP HẾT HẠN**\n\n");

                // Group by urgency
                List<Map<String, Object>> urgent = data.stream()
                        .filter(r -> safeParseInt(r.get("SoNgayConLai")) <= 30)
                        .collect(Collectors.toList());

                List<Map<String, Object>> warning = data.stream()
                        .filter(r -> {
                            int days = safeParseInt(r.get("SoNgayConLai"));
                            return days > 30 && days <= 90;
                        })
                        .collect(Collectors.toList());

                if (!urgent.isEmpty()) {
                    sb.append("🚨 **KHẨN CẤP (≤ 30 ngày):**\n");
                    urgent.forEach(record -> sb.append(formatSingleContract(record, true)));
                    sb.append("\n");
                }

                if (!warning.isEmpty()) {
                    sb.append("⚠️ **CẢnh BÁO (31-90 ngày):**\n");
                    warning.forEach(record -> sb.append(formatSingleContract(record, true)));
                    sb.append("\n");
                }

                // Summary
                sb.append(String.format("""
                📊 **TỔNG KẾT:**
                ├─ 🚨 Khẩn cấp: %d hợp đồng
                ├─ ⚠️ Cảnh báo: %d hợp đồng  
                └─ 📋 Tổng cộng: %d hợp đồng
                
                💡 **KHUYẾN NGHỊ:** Liên hệ với các nhân viên có hợp đồng sắp hết hạn để gia hạn hoặc thương thảo điều khoản mới.
                """, urgent.size(), warning.size(), data.size()));

            } else {
                // Regular contract list
                for (int i = 0; i < Math.min(data.size(), 20); i++) {
                    Map<String, Object> record = data.get(i);
                    sb.append(formatSingleContract(record, false));
                }

                if (data.size() > 20) {
                    sb.append(String.format("... và %d hợp đồng khác\n", data.size() - 20));
                }

                sb.append(String.format("\n📊 **Tổng cộng:** %d hợp đồng\n", data.size()));
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ formatContractReportSafe error: {}", e.getMessage());
            return formatGenericResponseSafe(data, dbResponse);
        }
    }

    // ✅ FORMAT SINGLE CONTRACT
    private String formatSingleContract(Map<String, Object> record, boolean showDaysLeft) {
        try {
            String maHopDong = safeGetString(record.get("MaHopDong"));
            String hoTen = safeGetString(record.get("HoTen"));
            String loaiHopDong = safeGetString(record.get("LoaiHopDong"));
            String ngayBatDau = formatDateSafe(record.get("NgayBatDau"));
            String ngayKetThuc = formatDateSafe(record.get("NgayKetThuc"));
            BigDecimal luongCoBan = getBigDecimal(record.get("LuongCoBan"));

            StringBuilder sb = new StringBuilder();

            if (showDaysLeft) {
                int soNgayConLai = safeParseInt(record.get("SoNgayConLai"));
                String urgencyIcon = soNgayConLai <= 7 ? "🔴" :
                        soNgayConLai <= 30 ? "🟡" : "🟢";

                sb.append(String.format("📋 **%s** %s\n", maHopDong, urgencyIcon));
                sb.append(String.format("├─ 👤 Nhân viên: %s\n", hoTen));
                sb.append(String.format("├─ 📝 Loại: %s\n", loaiHopDong));
                sb.append(String.format("├─ 📅 Kết thúc: %s\n", ngayKetThuc));
                sb.append(String.format("├─ ⏰ Còn lại: %d ngày\n", soNgayConLai));
                sb.append(String.format("└─ 💰 Lương: %s VNĐ\n\n", formatCurrency(luongCoBan)));
            } else {
                sb.append(String.format("📋 **%s**\n", maHopDong));
                sb.append(String.format("├─ 👤 Nhân viên: %s\n", hoTen));
                sb.append(String.format("├─ 📝 Loại: %s\n", loaiHopDong));
                sb.append(String.format("├─ 📅 Từ: %s → %s\n", ngayBatDau, ngayKetThuc));
                sb.append(String.format("└─ 💰 Lương: %s VNĐ\n\n", formatCurrency(luongCoBan)));
            }

            return sb.toString();
        } catch (Exception e) {
            return "❌ Lỗi hiển thị hợp đồng\n";
        }
    }
    // ✅ SAFE SALARY ANALYSIS (Fix line 278 error)
    private String formatSalaryAnalysisSafe(List<Map<String, Object>> data) {
        try {
            StringBuilder sb = new StringBuilder();

            // Detect if this is bonus/reward data or salary data
            Map<String, Object> firstRecord = data.get(0);
            boolean isBonusData = firstRecord.containsKey("TenThuong") || firstRecord.containsKey("MucThuong");
            boolean isTopSalary = firstRecord.containsKey("TongThuNhap");

            if (isBonusData) {
                sb.append("🎉 **PHÂN TÍCH THƯỞNG**\n\n");

                BigDecimal totalBonus = BigDecimal.ZERO;
                int bonusCount = 0;

                for (Map<String, Object> record : data) {
                    String hoTen = safeGetString(record.get("HoTen"));
                    String phongBan = safeGetString(record.get("TenPhongBan"));
                    String tenThuong = safeGetString(record.get("TenThuong"));
                    Object mucThuong = record.get("MucThuong");
                    Object ngayThuong = record.get("NgayThuong");
                    Object maNhanVien = record.get("MaNhanVien");
                    Object tongTienThuong = record.get("TongTienThuong");

                    BigDecimal bonusAmount = getBigDecimal(mucThuong != null ? mucThuong : tongTienThuong);
                    totalBonus = totalBonus.add(bonusAmount);
                    bonusCount++;

                    sb.append(String.format("""
                        🏆 **%s** (%s)
                        ├─ 🏢 Phòng ban: %s
                        ├─ 🎁 Loại thưởng: %s
                        ├─ 💰 Số tiền: %s VNĐ
                        └─ 📅 Ngày thưởng: %s
                        
                        """,
                            hoTen,
                            safeGetString(maNhanVien),
                            phongBan,
                            tenThuong,
                            formatCurrency(bonusAmount),
                            formatDate(ngayThuong)
                    ));
                }

                sb.append(String.format("""
                    📊 **TỔNG KẾT:**
                    ├─ 🎉 Số lượt thưởng: %d
                    ├─ 💰 Tổng tiền thưởng: %s VNĐ
                    └─ 📊 Thưởng trung bình: %s VNĐ
                    """,
                        bonusCount,
                        formatCurrency(totalBonus),
                        bonusCount > 0 ? formatCurrency(totalBonus.divide(BigDecimal.valueOf(bonusCount), 0, BigDecimal.ROUND_HALF_UP)) : "0"
                ));

            } else if (isTopSalary) {
                sb.append("💰 **PHÂN TÍCH TOP LƯƠNG**\n\n");

                BigDecimal totalSalary = BigDecimal.ZERO;
                int rank = 1;

                for (Map<String, Object> record : data) {
                    BigDecimal salary = getBigDecimal(record.get("TongThuNhap"));
                    totalSalary = totalSalary.add(salary);

                    String medal = switch (rank) {
                        case 1 -> "🥇";
                        case 2 -> "🥈";
                        case 3 -> "🥉";
                        default -> "🏆";
                    };

                    sb.append(String.format("""
                        %s **#%d - %s**
                        ├─ 💰 Lương: %s VNĐ
                        ├─ 🏢 Phòng ban: %s
                        ├─ 📅 Tháng: %s/%s
                        └─ 🆔 Mã NV: %s
                        
                        """,
                            medal, rank,
                            safeGetString(record.get("HoTen")),
                            formatCurrency(salary),
                            safeGetString(record.get("TenPhongBan")),
                            safeGetString(record.get("Thang")),
                            safeGetString(record.get("Nam")),
                            safeGetString(record.get("MaNhanVien"))
                    ));
                    rank++;
                }

                if (data.size() > 0) {
                    BigDecimal avgSalary = totalSalary.divide(BigDecimal.valueOf(data.size()), 0, BigDecimal.ROUND_HALF_UP);
                    sb.append(String.format("""
                        📊 **THỐNG KÊ:**
                        ├─ 💵 Tổng lương: %s VNĐ
                        ├─ 📊 Lương trung bình: %s VNĐ
                        └─ 👥 Số nhân viên: %d người
                        """,
                            formatCurrency(totalSalary),
                            formatCurrency(avgSalary),
                            data.size()
                    ));
                }
            } else {
                // Generic salary analysis
                sb.append("💰 **PHÂN TÍCH LƯƠNG KHÁC**\n\n");

                for (Map<String, Object> record : data) {
                    sb.append("📊 ");
                    for (Map.Entry<String, Object> entry : record.entrySet()) {
                        String key = translateColumnName(entry.getKey());
                        Object value = entry.getValue();
                        sb.append(String.format("**%s:** %s | ", key, formatValueSafe(value)));
                    }
                    sb.append("\n\n");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ formatSalaryAnalysisSafe error: {}", e.getMessage());
            return formatGenericResponseSafe(data, null);
        }
    }
    // ✅ ADD this method to fix compile errors
    private String formatDateSafe(Object dateObj) {
        try {
            if (dateObj == null) return "N/A";

            // Handle LocalDate
            if (dateObj instanceof LocalDate) {
                return ((LocalDate) dateObj).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            // Handle LocalDateTime
            if (dateObj instanceof LocalDateTime) {
                return ((LocalDateTime) dateObj).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            // Handle java.sql.Date
            if (dateObj instanceof java.sql.Date) {
                return ((java.sql.Date) dateObj).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            // Handle java.sql.Timestamp
            if (dateObj instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            // Handle java.util.Date
            if (dateObj instanceof java.util.Date) {
                return new java.sql.Date(((java.util.Date) dateObj).getTime())
                        .toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            // Try to parse string format
            String dateStr = dateObj.toString();
            if (dateStr.length() >= 10) {
                try {
                    return LocalDate.parse(dateStr.substring(0, 10))
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception e) {
                    // Try alternative parsing
                    return dateStr.substring(0, 10);
                }
            }

            return dateStr;
        } catch (Exception e) {
            log.warn("⚠️ Cannot format date: {} - Error: {}", dateObj, e.getMessage());
            return "N/A";
        }
    }
    // ✅ FIXED FINANCIAL ANALYSIS - Dynamic month detection
    private String formatFinancialAnalysisSafe(List<Map<String, Object>> data) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("💸 **PHÂN TÍCH TÀI CHÍNH**\n\n");

            if (!data.isEmpty()) {
                Map<String, Object> record = data.get(0);

                // Handle total fine amount
                Object tongTienPhat = record.get("TongTienPhat");
                if (tongTienPhat != null) {
                    BigDecimal fineAmount = getBigDecimal(tongTienPhat);

                    sb.append(String.format("💸 **Tổng tiền phạt:** %s VNĐ\n", formatCurrency(fineAmount)));

                    if (fineAmount.compareTo(BigDecimal.valueOf(1000000)) > 0) {
                        sb.append("⚠️ *Mức phạt cao, cần xem xét các biện pháp cải thiện*\n");
                    } else if (fineAmount.compareTo(BigDecimal.ZERO) == 0) {
                        sb.append("✅ *Không có vi phạm nào được ghi nhận*\n");
                    } else {
                        sb.append("🟡 *Mức phạt ở mức chấp nhận được*\n");
                    }
                } else {
                    // Handle comprehensive financial analysis
                    Object tongChiPhi = record.get("TongChiPhiLuong");
                    Object luongTB = record.get("LuongTrungBinh");
                    Object soNhanVien = record.get("SoNhanVien");
                    Object tongTangCa = record.get("TongTienTangCa");
                    Object tongThue = record.get("TongThue");

                    BigDecimal totalCost = getBigDecimal(tongChiPhi);
                    BigDecimal avgSalary = getBigDecimal(luongTB);
                    int employees = safeParseInt(soNhanVien);
                    BigDecimal totalOvertime = getBigDecimal(tongTangCa);
                    BigDecimal totalTax = getBigDecimal(tongThue);

                    BigDecimal costPerEmployee = employees > 0 ?
                            totalCost.divide(BigDecimal.valueOf(employees), 0, RoundingMode.HALF_UP) :
                            BigDecimal.ZERO;

                    double overtimeRatio = totalCost.compareTo(BigDecimal.ZERO) > 0 ?
                            totalOvertime.multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RoundingMode.HALF_UP).doubleValue() : 0.0;

                    // ✅ DYNAMIC MONTH/YEAR DETECTION
                    String periodLabel = detectPeriodFromData(record);

                    sb.append(String.format("""
                    💵 **CHI PHÍ %s:**
                    ├─ 💰 Tổng chi phí lương: %s VNĐ
                    ├─ 📊 Lương trung bình: %s VNĐ
                    ├─ 👥 Số nhân viên: %d người
                    ├─ ⏰ Tổng tiền tăng ca: %s VNĐ
                    ├─ 🏛️ Tổng thuế thu nhập: %s VNĐ
                    └─ 📈 Chi phí TB/người: %s VNĐ
                    
                    📊 **PHÂN TÍCH:**
                    ├─ Tỷ lệ overtime: %.1f%%
                    ├─ Đánh giá overtime: %s
                    └─ Chi phí/người: %s
                    
                    💡 **KHUYẾN NGHỊ:**
                    %s
                    
                    📅 *Cập nhật: %s*
                    """,
                            periodLabel,
                            formatCurrency(totalCost),
                            formatCurrency(avgSalary),
                            employees,
                            formatCurrency(totalOvertime),
                            formatCurrency(totalTax),
                            formatCurrency(costPerEmployee),
                            overtimeRatio,
                            getOvertimeAssessment(overtimeRatio),
                            getCostAssessment(costPerEmployee),
                            getRecommendation(overtimeRatio, costPerEmployee),
                            LocalDate.now().format(DATE_FORMATTER)
                    ));
                }
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ formatFinancialAnalysisSafe error: {}", e.getMessage());
            return formatGenericResponseSafe(data, null);
        }
    }

    // ✅ NEW METHOD: Detect period from data or context
    private String detectPeriodFromData(Map<String, Object> record) {
        try {
            // Try to detect from SQL context (stored in thread local or similar)
            String currentQuery = getCurrentSQLQuery(); // You'll need to implement this
            if (currentQuery != null) {
                // Extract month/year from SQL: "WHERE Thang = 4 AND Nam = 2025"
                if (currentQuery.contains("Thang = ") && currentQuery.contains("Nam = ")) {
                    String month = extractValueAfter(currentQuery, "Thang = ");
                    String year = extractValueAfter(currentQuery, "Nam = ");
                    if (month != null && year != null) {
                        return String.format("THÁNG %s/%s", month, year);
                    }
                }
            }

            // Fallback: Check if data contains month/year info
            Object thang = record.get("Thang");
            Object nam = record.get("Nam");
            if (thang != null && nam != null) {
                return String.format("THÁNG %s/%s", thang, nam);
            }

            // Default fallback
            return "HIỆN TẠI";

        } catch (Exception e) {
            log.warn("⚠️ Cannot detect period, using default");
            return "HIỆN TẠI";
        }
    }

    // ✅ HELPER METHODS
    private String getCurrentSQLQuery() {
        // TODO: Implement thread-local storage for current SQL
        // For now, return null - you can store this in ThreadLocal or request context
        return null;
    }

    private String extractValueAfter(String text, String pattern) {
        try {
            int index = text.indexOf(pattern);
            if (index != -1) {
                String after = text.substring(index + pattern.length()).trim();
                // Extract number until space or AND
                String[] parts = after.split("\\s+");
                if (parts.length > 0) {
                    return parts[0];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // ✅ SAFE ATTENDANCE REPORT
    private String formatAttendanceReportSafe(List<Map<String, Object>> data) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("📊 **BÁO CÁO CHẤM CÔNG**\n\n");

            int rank = 1;
            for (Map<String, Object> record : data) {
                String hoTen = safeGetString(record.get("HoTen"));
                String phongBan = safeGetString(record.get("TenPhongBan"));
                Object soLanViPham = record.get("SoLanViPham");
                if (soLanViPham == null) soLanViPham = record.get("SoLanMuon");

                int violationCount = safeParseInt(soLanViPham);
                String severity = getLateSeverity(violationCount);

                String rankEmoji = switch (rank) {
                    case 1 -> "🥇";
                    case 2 -> "🥈";
                    case 3 -> "🥉";
                    default -> "⚠️";
                };

                sb.append(String.format("""
                    %s **#%d - %s**
                    ├─ 🏢 Phòng ban: %s
                    ├─ 🆔 Mã NV: %s  
                    ├─ 📊 Số lần vi phạm: %d lần
                    └─ 🚨 Mức độ: %s
                    
                    """,
                        rankEmoji,
                        rank,
                        hoTen,
                        phongBan,
                        safeGetString(record.get("MaNhanVien")),
                        violationCount,
                        severity
                ));
                rank++;
            }

            int needAttention = (int) data.stream().mapToInt(r -> safeParseInt(r.get("SoLanViPham") != null ? r.get("SoLanViPham") : r.get("SoLanMuon"))).filter(x -> x >= 5).count();
            int serious = (int) data.stream().mapToInt(r -> safeParseInt(r.get("SoLanViPham") != null ? r.get("SoLanViPham") : r.get("SoLanMuon"))).filter(x -> x >= 10).count();

            sb.append(String.format("""
                📈 **THỐNG KÊ:**
                ├─ 👥 Tổng nhân viên vi phạm: %d người
                ├─ ⚠️ Cần chú ý (≥5 lần): %d người
                └─ 🚨 Nghiêm trọng (≥10 lần): %d người
                
                💡 **KHUYẾN NGHỊ:** Cần có biện pháp quản lý chấm công tốt hơn.
                """,
                    data.size(),
                    needAttention,
                    serious
            ));

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ formatAttendanceReportSafe error: {}", e.getMessage());
            return formatGenericResponseSafe(data, null);
        }
    }

    // ✅ SAFE COMPANY STATS
    private String formatCompanyStatsSafe(List<Map<String, Object>> data) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("🏢 **THỐNG KÊ TỔNG QUAN CÔNG TY**\n\n");

            for (Map<String, Object> stat : data) {
                String metric = safeGetString(stat.get("Metric"));
                Object value = stat.get("Value");

                String emoji = switch (metric) {
                    case "TongNhanVien" -> "👥";
                    case "TongPhongBan" -> "🏢";
                    case "LuongTrungBinh" -> "💰";
                    case "TongChiPhiLuong" -> "💵";
                    default -> "📊";
                };

                String displayName = switch (metric) {
                    case "TongNhanVien" -> "Tổng nhân viên đang làm việc";
                    case "TongPhongBan" -> "Tổng số phòng ban";
                    case "LuongTrungBinh" -> "Lương trung bình tháng 6/2025";
                    case "TongChiPhiLuong" -> "Tổng chi phí lương tháng 6/2025";
                    default -> metric;
                };

                String formattedValue = metric.contains("Luong") ? formatCurrency(value) : formatNumber(value);
                String unit = metric.contains("Luong") ? " VNĐ" :
                        metric.equals("TongNhanVien") ? " người" :
                                metric.equals("TongPhongBan") ? " phòng" : "";

                sb.append(String.format("%s **%s:** %s%s\n", emoji, displayName, formattedValue, unit));
            }

            sb.append(String.format("""
                
                📈 **ĐÁNH GIÁ:**
                ├─ 🟢 Tình hình nhân sự: Ổn định
                ├─ 💼 Cơ cấu tổ chức: Đầy đủ
                └─ 💰 Chi phí lương: Trong tầm kiểm soát
                
                📅 *Cập nhật: %s*
                """,
                    LocalDate.now().format(DATE_FORMATTER)
            ));

            return sb.toString();
        } catch (Exception e) {
            log.error("❌ formatCompanyStatsSafe error: {}", e.getMessage());
            return formatGenericResponseSafe(data, null);
        }
    }

    // ✅ SMART AI RESPONSE (Auto-detect data type)
    private String formatAIGeneratedResponseSmart(List<Map<String, Object>> data, DatabaseResponse dbResponse) {
        if (data.isEmpty()) {
            return "📭 Không tìm thấy dữ liệu.";
        }

        Map<String, Object> firstRow = data.get(0);

        // Contract data detection
        if (firstRow.containsKey("MaHopDong") || firstRow.containsKey("LoaiHopDong") ||
                firstRow.containsKey("NgayBatDau") || firstRow.containsKey("NgayKetThuc")) {
            log.info("🔧 AUTO-DETECT: Contract data found");
            return formatContractDataSmart(data);
        }

        // Salary data detection
        if (firstRow.containsKey("TongThuNhap") || firstRow.containsKey("TienTangCa")) {
            log.info("🔧 AUTO-DETECT: Salary data found");
            return formatSalaryInfo(data, permissionService.getCurrentUserName());
        }

        // Employee data detection
        if (firstRow.containsKey("HoTen") && firstRow.containsKey("Email")) {
            log.info("🔧 AUTO-DETECT: Employee data found");
            return formatEmployeeSearchResult(data);
        }

        // Default generic formatting
        log.info("🔧 AUTO-DETECT: Using generic formatting");
        return formatGenericResponseSafe(data, dbResponse);
    }

    // ✅ SMART CONTRACT FORMATTING
    private String formatContractDataSmart(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 **THÔNG TIN HỢP ĐỒNG**\n\n");

        for (Map<String, Object> contract : data) {
            String trangThai = safeGetString(contract.get("TrangThai"));
            String emoji = switch (trangThai.toLowerCase()) {
                case "còn hiệu lực", "con hieu luc", "active" -> "✅";
                case "hết hạn", "het han", "expired" -> "⏰";
                case "chấm dứt", "cham dut", "terminated" -> "❌";
                default -> "📄";
            };

            sb.append(String.format("""
                📄 **Hợp đồng #%s** %s
                ├─ 👤 Mã nhân viên: %s
                ├─ 👤 Tên nhân viên: %s
                ├─ 📋 Loại hợp đồng: %s
                ├─ 📅 Ngày bắt đầu: %s
                ├─ 📅 Ngày kết thúc: %s
                ├─ 💰 Lương cơ bản: %s VNĐ
                └─ 📊 Trạng thái: %s %s
                
                """,
                    safeGetString(contract.get("MaHopDong")),
                    emoji,
                    safeGetString(contract.get("MaNhanVien")),
                    safeGetString(contract.get("HoTen")),
                    safeGetString(contract.get("LoaiHopDong")),
                    formatDate(contract.get("NgayBatDau")),
                    formatDate(contract.get("NgayKetThuc")),
                    formatCurrency(contract.get("LuongCoBan")),
                    trangThai,
                    emoji
            ));
        }

        if (data.size() > 1) {
            sb.append(String.format("📊 **Tổng cộng:** %d hợp đồng\n", data.size()));
        }

        return sb.toString();
    }

    // ✅ GENERIC SAFE FORMATTER
    private String formatGenericResponseSafe(List<Map<String, Object>> data, DatabaseResponse dbResponse) {
        StringBuilder sb = new StringBuilder();
        sb.append("🤖 **KẾT QUẢ TÌM KIẾM**\n\n");

        if (data.size() == 1) {
            // Single record - detailed view
            Map<String, Object> record = data.get(0);
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                String key = translateColumnName(entry.getKey());
                Object value = entry.getValue();
                sb.append(String.format("├─ **%s:** %s\n", key, formatValueSafe(value)));
            }
        } else {
            // Multiple records - summary view
            sb.append(String.format("📊 **Tìm thấy %d kết quả:**\n\n", data.size()));

            int count = 1;
            for (Map<String, Object> record : data) {
                sb.append(String.format("**%d.** ", count++));

                // Show key fields
                for (Map.Entry<String, Object> entry : record.entrySet()) {
                    if (isKeyField(entry.getKey())) {
                        sb.append(String.format("%s: %s | ",
                                translateColumnName(entry.getKey()),
                                formatValueSafe(entry.getValue())));
                    }
                }
                sb.append("\n");

                if (count > 15) {
                    sb.append(String.format("... và %d kết quả khác\n", data.size() - 15));
                    break;
                }
            }
        }

        return sb.toString();
    }

    private String formatValueSafe(Object value) {
        if (value == null) return "N/A";
        if (value instanceof Number) {
            return formatNumber(value);
        }
        return value.toString();
    }

    private String formatNumber(Object value) {
        if (value == null) return "0";
        double num = safeParseDouble(value);
        return String.format("%,.0f", num);
    }

    private String generateEmptyResultMessage(QueryType queryType) {
        return switch (queryType) {
            case MY_SALARY -> "💰 Chưa có thông tin lương cho thời gian này.";
            case MY_ATTENDANCE -> "📊 Chưa có dữ liệu chấm công.";
            case MY_LEAVE -> "🏖️ Chưa có đơn nghỉ phép nào.";
            case MY_CONTRACT -> "📋 Chưa có thông tin hợp đồng.";
            case MY_ALLOWANCE -> "💼 Chưa có thông tin phụ cấp.";
            case MY_BONUS -> "🎁 Chưa có thông tin thưởng.";
            case MY_VIOLATION -> "✅ Bạn chưa có vi phạm nào.";
            case TEAM_INFO -> "👥 Không tìm thấy thông tin team.";
            case EMPLOYEE_SEARCH -> "🔍 Không tìm thấy nhân viên nào phù hợp.";
            case ADMIN_ALL_EMPLOYEES -> "👥 Không có nhân viên nào trong hệ thống.";
            case ADMIN_SALARY_ANALYSIS -> "💰 Không có dữ liệu lương để phân tích.";
            case ADMIN_ATTENDANCE_REPORT -> "📊 Không có dữ liệu chấm công.";
            case ADMIN_COMPANY_STATS -> "📊 Không có dữ liệu thống kê.";
            case ADMIN_FINANCIAL_ANALYSIS -> "💰 Không có dữ liệu tài chính.";
            default -> "🔍 Không tìm thấy dữ liệu phù hợp với câu hỏi của bạn.";
        };
    }

    // ===== EXISTING FORMATTERS (with safe parsing) =====

    private String formatEmployeeSearchResult(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return "🔍 Không tìm thấy nhân viên nào phù hợp với tìm kiếm của bạn.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔍 **KẾT QUẢ TÌM KIẾM NHÂN VIÊN**\n\n");

        if (data.size() == 1) {
            // Single result - detailed view
            Map<String, Object> emp = data.get(0);
            sb.append(formatSingleEmployeeDetail(emp));
        } else {
            // Multiple results - summary view
            sb.append(String.format("📊 **Tìm thấy %d nhân viên:**\n\n", data.size()));

            for (Map<String, Object> emp : data) {
                sb.append(String.format("""
                    👤 **%s** (ID: %s)
                    ├─ 📧 Email: %s
                    ├─ 🏢 Phòng ban: %s  
                    ├─ 👔 Chức vụ: %s
                    └─ 📊 Trạng thái: %s
                    
                    """,
                        safeGetString(emp, "HoTen"),
                        safeGetString(emp, "MaNhanVien"),
                        safeGetString(emp, "Email"),
                        safeGetString(emp, "TenPhongBan"),
                        safeGetString(emp, "TenChucVu"),
                        safeGetString(emp, "TrangThaiLamViec")
                ));
            }
        }

        return sb.toString();
    }

    private String formatSingleEmployeeDetail(Map<String, Object> emp) {
        return String.format("""
            👤 **THÔNG TIN NHÂN VIÊN**
            
            🏷️ **Mã NV:** %s
            📛 **Họ tên:** %s
            ⚥ **Giới tính:** %s
            🎂 **Ngày sinh:** %s
            📧 **Email:** %s
            📱 **SĐT:** %s
            🏠 **Địa chỉ:** %s
            🎓 **Trình độ:** %s
            🏢 **Phòng ban:** %s
            👔 **Chức vụ:** %s
            💰 **Lương hiện tại:** %s VNĐ
            ⏰ **Thâm niên:** %s năm
            📊 **Trạng thái:** %s
            
            📅 *Tra cứu: %s*
            """,
                safeGetString(emp, "MaNhanVien"),
                safeGetString(emp, "HoTen"),
                safeGetString(emp, "GioiTinh"),
                formatDate(emp.get("NgaySinh")),
                safeGetString(emp, "Email"),
                safeGetString(emp, "SDT"),
                safeGetString(emp, "DiaChi"),
                safeGetString(emp, "TrinhDoHocVan"),
                safeGetString(emp, "TenPhongBan"),
                safeGetString(emp, "TenChucVu"),
                formatCurrency(emp.get("LuongHienTai")),
                safeGetString(emp, "ThamNien"),
                safeGetString(emp, "TrangThaiLamViec"),
                LocalDate.now().format(DATE_FORMATTER)
        );
    }

    private String formatAllEmployeesReport(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("👥 **DANH SÁCH TẤT CẢ NHÂN VIÊN**\n\n");

        Map<String, Integer> deptCounts = new HashMap<>();
        Map<String, Integer> statusCounts = new HashMap<>();

        for (Map<String, Object> emp : data) {
            String trangThai = safeGetString(emp, "TrangThaiLamViec");
            String phongBan = safeGetString(emp, "TenPhongBan");

            deptCounts.merge(phongBan.isEmpty() ? "Chưa phân công" : phongBan, 1, Integer::sum);
            statusCounts.merge(trangThai.isEmpty() ? "Không xác định" : trangThai, 1, Integer::sum);

            sb.append(String.format("""
                👤 **%s** (ID: %s)
                ├─ 📧 Email: %s
                ├─ 🏢 Phòng ban: %s  
                ├─ 👔 Chức vụ: %s
                ├─ 💰 Lương: %s VNĐ
                └─ 📊 Trạng thái: %s %s
                
                """,
                    safeGetString(emp, "HoTen"),
                    safeGetString(emp, "MaNhanVien"),
                    safeGetString(emp, "Email"),
                    phongBan.isEmpty() ? "Chưa phân công" : phongBan,
                    safeGetString(emp, "TenChucVu"),
                    formatCurrency(emp.get("LuongHienTai")),
                    trangThai,
                    "Đang làm việc".equals(trangThai) ? "✅" : "⚠️"
            ));
        }

        sb.append(String.format("""
            📊 **THỐNG KÊ TỔNG QUAN:**
            ├─ 👥 Tổng nhân viên: %d người
            ├─ ✅ Đang làm việc: %d người  
            ├─ ⚠️ Nghỉ việc/Khác: %d người
            └─ 🏢 Số phòng ban: %d phòng
            
            🏢 **PHÂN BỐ THEO PHÒNG BAN:**
            """,
                data.size(),
                statusCounts.getOrDefault("Đang làm việc", 0),
                data.size() - statusCounts.getOrDefault("Đang làm việc", 0),
                deptCounts.size()
        ));

        deptCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sb.append(String.format("├─ %s: %d người\n", entry.getKey(), entry.getValue())));

        return sb.toString();
    }

    private String formatAdminContractInfo(List<Map<String, Object>> data) {
        return formatContractDataSmart(data); // Use the smart contract formatter
    }

    // ===== KEEP ALL YOUR EXISTING PERSONAL FORMATTERS =====

    private String formatEmployeeInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "👤 Không tìm thấy thông tin nhân viên.";

        Map<String, Object> emp = data.get(0);

        return String.format("""
            👤 **THÔNG TIN CÁ NHÂN - %s**
            
            🏷️ **Mã NV:** %s
            📛 **Họ tên:** %s
            ⚥ **Giới tính:** %s
            🎂 **Ngày sinh:** %s
            📧 **Email:** %s
            📱 **SĐT:** %s
            🏠 **Địa chỉ:** %s
            🎓 **Trình độ:** %s
            🏖️ **Số ngày phép:** %s ngày
            ⏰ **Thâm niên:** %s năm
            💰 **Lương hiện tại:** %s VNĐ
            📊 **Trạng thái:** %s
            
            📅 *Cập nhật: %s*
            """,
                userName,
                safeGetString(emp, "MaNhanVien"),
                safeGetString(emp, "HoTen"),
                safeGetString(emp, "GioiTinh"),
                formatDate(emp.get("NgaySinh")),
                safeGetString(emp, "Email"),
                safeGetString(emp, "SDT"),
                safeGetString(emp, "DiaChi"),
                safeGetString(emp, "TrinhDoHocVan"),
                safeGetString(emp, "SoNgayPhep"),
                safeGetString(emp, "ThamNien"),
                formatCurrency(emp.get("LuongHienTai")),
                safeGetString(emp, "TrangThaiLamViec"),
                LocalDate.now().format(DATE_FORMATTER)
        );
    }

    private String formatSalaryInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "💰 Chưa có thông tin lương.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("💰 **THÔNG TIN LƯƠNG - %s**\n\n", userName));

        BigDecimal totalSalary = BigDecimal.ZERO;

        for (Map<String, Object> salary : data) {
            BigDecimal tongThuNhap = getBigDecimal(salary.get("TongThuNhap"));
            totalSalary = totalSalary.add(tongThuNhap);

            sb.append(String.format("""
                📅 **Tháng %s/%s:**
                ├─ 💵 Tổng thu nhập: %s VNĐ
                ├─ ⏰ Tiền tăng ca: %s VNĐ
                ├─ 👥 Người phụ thuộc: %s người
                ├─ 🏛️ Thuế thu nhập: %s VNĐ
                └─ 📅 Ngày nhận: %s
                
                """,
                    safeGetString(salary, "Thang"),
                    safeGetString(salary, "Nam"),
                    formatCurrency(tongThuNhap),
                    formatCurrency(salary.get("TienTangCa")),
                    safeGetString(salary, "SoNguoiPhuThuoc"),
                    formatCurrency(salary.get("ThueThuNhap")),
                    formatDate(salary.get("NgayNhan"))
            ));
        }

        if (data.size() > 1) {
            sb.append(String.format("💰 **Tổng thu nhập: %s VNĐ**", formatCurrency(totalSalary)));
        }

        return sb.toString();
    }

    private String formatAttendanceInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "📊 Chưa có dữ liệu chấm công.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 **CHẤM CÔNG - %s**\n\n", userName));

        int lateCount = 0;
        double totalOvertimeHours = 0.0;

        for (Map<String, Object> record : data) {
            String trangThai = safeGetString(record, "TrangThai");
            if ("Muộn".equals(trangThai) || "Late".equals(trangThai)) {
                lateCount++;
            }

            double overtimeHours = safeParseDouble(record.get("SoGioTangCa"));
            totalOvertimeHours += overtimeHours;

            sb.append(String.format("""
                📅 **%s:**
                ├─ ⏰ Vào: %s | Ra: %s
                ├─ ✅ Trạng thái: %s %s
                └─ ⏱️ Tăng ca: %.1f giờ
                
                """,
                    formatDate(record.get("Ngay")),
                    safeGetString(record, "GioVao"),
                    safeGetString(record, "GioRa"),
                    trangThai,
                    "Muộn".equals(trangThai) || "Late".equals(trangThai) ? "⚠️" : "✅",
                    overtimeHours
            ));
        }

        sb.append(String.format("""
            📈 **THỐNG KÊ:**
            ├─ 📊 Tổng ngày: %d ngày
            ├─ ⚠️ Số lần đi muộn: %d lần
            ├─ 📊 Tỷ lệ đúng giờ: %.1f%%
            └─ ⏱️ Tổng tăng ca: %.1f giờ
            """,
                data.size(),
                lateCount,
                data.size() > 0 ? (double)(data.size() - lateCount) / data.size() * 100 : 100.0,
                totalOvertimeHours
        ));

        return sb.toString();
    }

    private String formatLeaveInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "🏖️ Chưa có thông tin nghỉ phép.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🏖️ **NGHỈ PHÉP - %s**\n\n", userName));

        for (Map<String, Object> leave : data) {
            String trangThai = safeGetString(leave, "TrangThaiPheDuyet");
            String emoji = switch (trangThai) {
                case "Đã duyệt" -> "✅";
                case "Chờ duyệt" -> "⏳";
                case "Từ chối" -> "❌";
                default -> "📋";
            };

            sb.append(String.format("""
                📋 **Đơn #%s:** %s
                ├─ 📅 Từ: %s → %s
                ├─ 📝 Lý do: %s
                └─ 📊 Trạng thái: %s %s
                
                """,
                    safeGetString(leave, "MaNghiPhep"),
                    emoji,
                    formatDate(leave.get("NgayBatDau")),
                    formatDate(leave.get("NgayKetThuc")),
                    safeGetString(leave, "LyDo"),
                    trangThai,
                    emoji
            ));
        }

        return sb.toString();
    }

    private String formatContractInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "📋 Chưa có thông tin hợp đồng.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📋 **HỢP ĐỒNG - %s**\n\n", userName));

        for (Map<String, Object> contract : data) {
            String trangThai = safeGetString(contract, "TrangThai");
            String emoji = switch (trangThai) {
                case "Còn hiệu lực" -> "✅";
                case "Hết hạn" -> "⏰";
                case "Chấm dứt" -> "❌";
                default -> "📄";
            };

            sb.append(String.format("""
                📄 **Hợp đồng #%s:** %s
                ├─ 📋 Loại: %s
                ├─ 📅 Thời hạn: %s → %s
                ├─ 💰 Lương cơ bản: %s VNĐ
                └─ 📊 Trạng thái: %s %s
                
                """,
                    safeGetString(contract, "MaHopDong"),
                    emoji,
                    safeGetString(contract, "LoaiHopDong"),
                    formatDate(contract.get("NgayBatDau")),
                    formatDate(contract.get("NgayKetThuc")),
                    formatCurrency(contract.get("LuongCoBan")),
                    trangThai,
                    emoji
            ));
        }

        return sb.toString();
    }

    private String formatAllowanceInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "💼 Chưa có thông tin phụ cấp.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("💼 **PHỤ CẤP - %s**\n\n", userName));

        BigDecimal totalAllowance = BigDecimal.ZERO;

        for (Map<String, Object> allowance : data) {
            BigDecimal mucTien = getBigDecimal(allowance.get("MucTien"));
            totalAllowance = totalAllowance.add(mucTien);

            sb.append(String.format("""
                💰 **%s:**
                ├─ 💵 Mức tiền: %s VNĐ
                ├─ 📅 Từ ngày: %s
                └─ 📅 Đến ngày: %s
                
                """,
                    safeGetString(allowance, "TenPhuCap"),
                    formatCurrency(mucTien),
                    formatDate(allowance.get("NgayBatDau")),
                    formatDate(allowance.get("NgayKetThuc"))
            ));
        }

        sb.append(String.format("💰 **Tổng phụ cấp:** %s VNĐ/tháng", formatCurrency(totalAllowance)));

        return sb.toString();
    }

    private String formatBonusInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "🎁 Chưa có thông tin thưởng.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🎁 **THƯỞNG - %s**\n\n", userName));

        BigDecimal totalBonus = BigDecimal.ZERO;

        for (Map<String, Object> bonus : data) {
            Object mucTienObj = bonus.get("MucTien");
            if (mucTienObj == null) mucTienObj = bonus.get("MucThuong");

            BigDecimal mucTien = getBigDecimal(mucTienObj);
            totalBonus = totalBonus.add(mucTien);

            sb.append(String.format("""
                🏆 **%s:**
                ├─ 💰 Số tiền: %s VNĐ
                └─ 📅 Ngày thưởng: %s
                
                """,
                    safeGetString(bonus, "TenThuong"),
                    formatCurrency(mucTien),
                    formatDate(bonus.get("NgayThuong"))
            ));
        }

        sb.append(String.format("🎊 **Tổng thưởng:** %s VNĐ", formatCurrency(totalBonus)));

        return sb.toString();
    }

    private String formatViolationInfo(List<Map<String, Object>> data, String userName) {
        if (data.isEmpty()) return "✅ Bạn chưa có vi phạm nào.";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🚫 **VI PHẠM - %s**\n\n", userName));

        BigDecimal totalFine = BigDecimal.ZERO;

        for (Map<String, Object> violation : data) {
            BigDecimal soTienPhat = getBigDecimal(violation.get("SoTienPhat"));
            totalFine = totalFine.add(soTienPhat);

            sb.append(String.format("""
                ⚠️ **Vi phạm #%s:**
                ├─ 🚫 Loại: %s
                ├─ 📅 Ngày: %s
                ├─ 📝 Mô tả: %s
                ├─ ⚖️ Hình thức phạt: %s
                ├─ 💰 Số tiền phạt: %s VNĐ
                └─ 👤 Người quyết định: %s
                
                """,
                    safeGetString(violation, "MaViPham"),
                    safeGetString(violation, "LoaiViPham"),
                    formatDate(violation.get("NgayViPham")),
                    safeGetString(violation, "MoTa"),
                    safeGetString(violation, "HinhThucPhat"),
                    formatCurrency(soTienPhat),
                    safeGetString(violation, "NguoiRaQuyetDinh")
            ));
        }

        sb.append(String.format("💸 **Tổng tiền phạt:** %s VNĐ", formatCurrency(totalFine)));

        return sb.toString();
    }

    private String formatTeamInfo(List<Map<String, Object>> data) {
        if (data.isEmpty()) return "👥 Không có thông tin team.";

        StringBuilder sb = new StringBuilder();
        sb.append("👥 **THÔNG TIN TEAM**\n\n");

        for (Map<String, Object> member : data) {
            sb.append(String.format("• **%s** - %s - %s\n",
                    safeGetString(member, "HoTen"),
                    safeGetString(member, "TenChucVu"),
                    safeGetString(member, "Email")
            ));
        }

        sb.append(String.format("\n👥 **Tổng cộng:** %d thành viên", data.size()));

        return sb.toString();
    }

    // ===== HELPER METHODS =====

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "N/A";

        try {
            if (dateObj instanceof LocalDate) {
                return ((LocalDate) dateObj).format(DATE_FORMATTER);
            } else if (dateObj instanceof LocalDateTime) {
                return ((LocalDateTime) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.sql.Date) {
                return ((java.sql.Date) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate().format(DATE_FORMATTER);
            }
            return dateObj.toString();
        } catch (Exception e) {
            log.warn("⚠️ Cannot format date: '{}', returning as string", dateObj);
            return dateObj.toString();
        }
    }

    private String formatCurrency(Object amount) {
        if (amount == null) return "0";

        BigDecimal value = getBigDecimal(amount);
        return String.format("%,d", value.longValue());
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;

        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            } else if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                String str = value.toString().trim();
                if ("N/A".equals(str) || str.isEmpty()) return BigDecimal.ZERO;
                return new BigDecimal(str);
            }
        } catch (Exception e) {
            log.warn("⚠️ Cannot parse as BigDecimal: '{}', returning 0", value);
            return BigDecimal.ZERO;
        }
    }

    private String getLateSeverity(int lateCount) {
        if (lateCount >= 10) return "🚨 Nghiêm trọng";
        if (lateCount >= 5) return "⚠️ Cảnh báo";
        if (lateCount >= 2) return "🟡 Chú ý";
        return "🟢 Bình thường";
    }

    private double calculateOvertimeRatio(Map<String, Object> analysis) {
        BigDecimal totalSalary = getBigDecimal(analysis.get("TongChiPhiLuong"));
        BigDecimal totalOvertime = getBigDecimal(analysis.get("TongTienTangCa"));

        if (totalSalary.compareTo(BigDecimal.ZERO) > 0) {
            return totalOvertime.multiply(BigDecimal.valueOf(100))
                    .divide(totalSalary, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return 0.0;
    }

    private String getOvertimeAssessment(double overtimeRatio) {
        if (overtimeRatio > 20) return "Quá cao - cần xem xét";
        if (overtimeRatio > 10) return "Cao - theo dõi";
        if (overtimeRatio > 5) return "Bình thường";
        return "Thấp - tốt";
    }

    private String getCostAssessment(BigDecimal costPerEmployee) {
        long cost = costPerEmployee.longValue();
        if (cost > 50000000) return "Cao";
        if (cost > 30000000) return "Trung bình";
        return "Hợp lý";
    }

    private String getRecommendation(double overtimeRatio, BigDecimal costPerEmployee) {
        if (overtimeRatio > 15) {
            return "• Xem xét tối ưu hóa quy trình làm việc\n• Tuyển thêm nhân viên nếu cần thiết";
        }
        if (costPerEmployee.longValue() > 40000000) {
            return "• Đánh giá lại cơ cấu lương\n• Tối ưu hóa chi phí nhân sự";
        }
        return "• Duy trì hiệu quả hiện tại\n• Theo dõi các chỉ số định kỳ";}
    // ✅ ADD MISSING METHODS
    private String translateColumnName(String columnName) {
        return switch (columnName.toLowerCase()) {
            // Employee columns
            case "manhanvien" -> "Mã nhân viên";
            case "hoten" -> "Họ tên";
            case "email" -> "Email";
            case "gioitinh" -> "Giới tính";
            case "ngaysinh" -> "Ngày sinh";
            case "sdt" -> "SĐT";
            case "diachi" -> "Địa chỉ";
            case "trinhdohocvan" -> "Trình độ";
            case "luonghientai" -> "Lương hiện tại";
            case "trangthai" -> "Trạng thái";
            case "trangthailamviec" -> "Trạng thái làm việc";

            // Contract columns
            case "mahopdong" -> "Mã hợp đồng";
            case "loaihopdong" -> "Loại hợp đồng";
            case "ngaybatdau" -> "Ngày bắt đầu";
            case "ngayketthuc" -> "Ngày kết thúc";
            case "luongcoban" -> "Lương cơ bản";

            // Department columns
            case "tenphongban" -> "Tên phòng ban";
            case "tenchucvu" -> "Chức vụ";
            case "tongsogioteangca" -> "Tổng giờ tăng ca";
            case "tongtienthuong" -> "Tổng tiền thưởng";
            case "tongtinephat" -> "Tổng tiền phạt";

            // Bonus columns
            case "tenthuong" -> "Tên thưởng";
            case "mucthuong" -> "Mức thưởng";
            case "ngaythuong" -> "Ngày thưởng";

            // Salary columns
            case "tongthuenhap" -> "Tổng thu nhập";
            case "tientangca" -> "Tiền tăng ca";
            case "thuethunhap" -> "Thuế thu nhập";
            case "thang" -> "Tháng";
            case "nam" -> "Năm";

            // Violation columns
            case "solanvipham" -> "Số lần vi phạm";
            case "loaivipham" -> "Loại vi phạm";
            case "sotienphat" -> "Số tiền phạt";
            case "ngayvipham" -> "Ngày vi phạm";

            default -> columnName;
        };
    }
    private boolean isKeyField(String columnName) {
        String lower = columnName.toLowerCase();
        return lower.contains("hoten") || lower.contains("ten") ||
                lower.contains("ma") || lower.contains("luong") ||
                lower.contains("email") || lower.contains("chucvu") ||
                lower.contains("phongban") || lower.contains("ngay") ||
                lower.contains("trang") || lower.contains("loai") ||
                lower.contains("muc") || lower.contains("so");
    }
    @SuppressWarnings("unused")
    private long safeParseLongWrapper(Object value) {
        return safeParseLong(value);
    }
}
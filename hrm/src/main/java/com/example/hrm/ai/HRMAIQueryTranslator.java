package com.example.hrm.ai;

import com.example.hrm.domain.NhanVien;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class HRMAIQueryTranslator {

    private static final Logger log = LoggerFactory.getLogger(HRMAIQueryTranslator.class);

    // ✅ CONSTRUCTOR INJECTION - Fixed field injection warnings
    private final GeminiAIService geminiAIService;
    private final HRMPermissionService permissionService;

    public HRMAIQueryTranslator(GeminiAIService geminiAIService, HRMPermissionService permissionService) {
        this.geminiAIService = geminiAIService;
        this.permissionService = permissionService;
    }

    public QueryResult translateToSQL(String naturalLanguageQuery) {
        try {
            NhanVien currentUser = permissionService.getCurrentUser();
            if (currentUser == null) {
                return QueryResult.builder()
                        .queryType(QueryType.PERMISSION_DENIED)
                        .originalQuestion(naturalLanguageQuery)
                        .build();
            }

            UserRole userRole = permissionService.getCurrentUserRole();
            String databaseSchema = getDatabaseSchemaForRole(userRole);
            String securityContext = permissionService.getSecurityContext();

            String prompt = buildTranslationPrompt(naturalLanguageQuery, databaseSchema, securityContext, userRole, currentUser);

            log.info("🔄 Translating query: {} | User: {} ({}) | Role: {}",
                    naturalLanguageQuery, currentUser.getHoTen(), currentUser.getId(), userRole);

            String aiResponse = geminiAIService.generateContent(prompt);
            log.info("🤖 AI Response: {}", aiResponse);

            return parseAIResponse(aiResponse, naturalLanguageQuery, currentUser, userRole);

        } catch (Exception e) {
            log.error("❌ Translation error: {}", e.getMessage(), e);
            return QueryResult.builder()
                    .queryType(QueryType.UNKNOWN)
                    .originalQuestion(naturalLanguageQuery)
                    .build();
        }
    }

    private String buildTranslationPrompt(String query, String schema, String securityContext, UserRole role, NhanVien currentUser) {
        String userName = currentUser.getHoTen();
        Integer employeeId = currentUser.getId();
        // ✅ REMOVED unused variable 'chucVu' - Fixed warning
        Integer deptId = currentUser.getChucVu() != null ? currentUser.getChucVu().getPhongBan().getId() : null;

        return String.format("""
        BẠN LÀ CHUYÊN GIA CHUYỂN ĐỔI NATURAL LANGUAGE SANG SQL CHO HỆ THỐNG HRM.
        
        ⚠️ DATABASE: SQL SERVER - DÙNG TOP THAY VÌ LIMIT
        ⚠️ CẤM TUYỆT ĐỐI: Không được dùng newline (\\n) trong SQL! Chỉ dùng dấu cách.
        
        THÔNG TIN USER HIỆN TẠI:
        - Tên: %s
        - ID: %d  
        - Login: trung2710
        - Vai trò: %s
        - Phòng ban ID: %s
        - Ngày hiện tại: 2025-06-21
        
        SCHEMA: %s
        
        CÂU HỎI: "%s"
        
        CÁC VÍ DỤ CHUYỂN ĐỔI THEO VAI TRÒ:
        
        === NHÂN VIÊN (EMPLOYEE) ===
        1. "Lương tháng 4 của tôi"
           → SQL: SELECT TOP 10 * FROM Luong WHERE MaNhanVien = %d AND Thang = 4 AND Nam = 2025
           → TYPE: MY_SALARY

        2. "Thưởng của tôi" hoặc "thưởng tháng này"
           → SQL: SELECT TOP 10 nv.*, t.TenThuong, t.MucThuong FROM NhanVien_Thuong nv JOIN Thuong t ON nv.MaThuong = t.MaThuong WHERE nv.MaNhanVien = %d ORDER BY nv.NgayThuong DESC
           → TYPE: MY_BONUS

        3. "Vi phạm của tôi" hoặc "tôi có vi phạm gì"
           → SQL: SELECT TOP 10 nv.*, vp.LoaiViPham, vp.HinhThucPhat, vp.SoTienPhat FROM NhanVien_ViPham nv JOIN ViPham vp ON nv.MaViPham = vp.MaViPham WHERE nv.MaNhanVien = %d ORDER BY nv.NgayViPham DESC
           → TYPE: MY_VIOLATION

        4. "Thông tin cá nhân của tôi"
           → SQL: SELECT TOP 1 * FROM NhanVien WHERE MaNhanVien = %d  
           → TYPE: MY_INFO

        5. "Chấm công tháng này"
           → SQL: SELECT TOP 50 * FROM ChamCong WHERE MaNhanVien = %d AND MONTH(Ngay) = 6 AND YEAR(Ngay) = 2025
           → TYPE: MY_ATTENDANCE

        6. "Hợp đồng của tôi"
           → SQL: SELECT TOP 10 * FROM HopDong WHERE MaNhanVien = %d ORDER BY NgayBatDau DESC
           → TYPE: MY_CONTRACT

        === ADMIN (Full Access) ===
        7. "Thông tin của nhân viên tên là Nguyễn Văn An"
           → SQL: SELECT TOP 5 nv.*, cv.TenChucVu, pb.TenPhongBan FROM NhanVien nv LEFT JOIN ChucVu cv ON nv.MaChucVu = cv.MaChucVu LEFT JOIN PhongBan pb ON cv.MaPhongBan = pb.MaPhongBan WHERE nv.HoTen LIKE N'%%Nguyễn Văn An%%'
           → TYPE: EMPLOYEE_SEARCH

        8. "Danh sách tất cả nhân viên trong công ty"
           → SQL: SELECT TOP 100 nv.MaNhanVien, nv.HoTen, nv.Email, nv.TrangThaiLamViec, cv.TenChucVu, pb.TenPhongBan, nv.LuongHienTai FROM NhanVien nv LEFT JOIN ChucVu cv ON nv.MaChucVu = cv.MaChucVu LEFT JOIN PhongBan pb ON cv.MaPhongBan = pb.MaPhongBan ORDER BY nv.MaNhanVien
           → TYPE: ADMIN_ALL_EMPLOYEES

        9. "Nhân viên nào được thưởng nhiều nhất năm nay?"
           → SQL: SELECT TOP 10 nv.MaNhanVien, nv.HoTen, pb.TenPhongBan, SUM(nt.MucTien) AS TongTienThuong FROM NhanVien nv JOIN NhanVien_Thuong nt ON nv.MaNhanVien = nt.MaNhanVien LEFT JOIN ChucVu cv ON nv.MaChucVu = cv.MaChucVu LEFT JOIN PhongBan pb ON cv.MaPhongBan = pb.MaPhongBan WHERE YEAR(nt.NgayThuong) = YEAR(GETDATE()) GROUP BY nv.MaNhanVien, nv.HoTen, pb.TenPhongBan ORDER BY TongTienThuong DESC
           → TYPE: ADMIN_SALARY_ANALYSIS

        10. "Lịch sử thưởng năm 2025"
            → SQL: SELECT nv.*, t.TenThuong, t.MucThuong FROM NhanVien_Thuong nv JOIN Thuong t ON nv.MaThuong = t.MaThuong WHERE YEAR(nv.NgayThuong) = 2025 ORDER BY nv.NgayThuong DESC
            → TYPE: ADMIN_SALARY_ANALYSIS

        11. "Tiền phạt tổng cộng"
            → SQL: SELECT SUM(vp.SoTienPhat) AS TongTienPhat FROM NhanVien_ViPham nv JOIN ViPham vp ON nv.MaViPham = vp.MaViPham
            → TYPE: ADMIN_FINANCIAL_ANALYSIS

        12. "Thống kê tăng ca theo phòng ban"
            → SQL: SELECT pb.TenPhongBan, SUM(cc.SoGioTangCa) AS TongSoGioTangCa FROM PhongBan pb JOIN ChucVu cv ON pb.MaPhongBan = cv.MaPhongBan JOIN NhanVien nv ON cv.MaChucVu = nv.MaChucVu JOIN ChamCong cc ON nv.MaNhanVien = cc.MaNhanVien WHERE MONTH(cc.Ngay) = 6 AND YEAR(cc.Ngay) = 2025 GROUP BY pb.MaPhongBan, pb.TenPhongBan ORDER BY TongSoGioTangCa DESC
            → TYPE: ADMIN_DEPARTMENT_REPORT

        13. "Nhân viên vi phạm nhiều nhất"
            → SQL: SELECT TOP 10 nv.MaNhanVien, nv.HoTen, pb.TenPhongBan, COUNT(*) AS SoLanViPham FROM NhanVien nv JOIN NhanVien_ViPham nvp ON nv.MaNhanVien = nvp.MaNhanVien LEFT JOIN ChucVu cv ON nv.MaChucVu = cv.MaChucVu LEFT JOIN PhongBan pb ON cv.MaPhongBan = pb.MaPhongBan GROUP BY nv.MaNhanVien, nv.HoTen, pb.TenPhongBan ORDER BY SoLanViPham DESC
            → TYPE: ADMIN_ATTENDANCE_REPORT

        14. "Hợp đồng của nhân viên tên Nguyễn Minh Sáng"
            → SQL: SELECT TOP 10 hd.*, nv.HoTen FROM HopDong hd JOIN NhanVien nv ON hd.MaNhanVien = nv.MaNhanVien WHERE nv.HoTen LIKE N'%%Nguyễn Minh Sáng%%' ORDER BY hd.NgayBatDau DESC
            → TYPE: ADMIN_CONTRACT_REPORT

        15. "Báo cáo lương theo phòng ban tháng 5/2025"
            → SQL: SELECT pb.TenPhongBan, COUNT(DISTINCT nv.MaNhanVien) as SoNhanVien, AVG(l.TongThuNhap) as LuongTrungBinh, SUM(l.TongThuNhap) as TongLuong, MIN(l.TongThuNhap) as LuongThapNhat, MAX(l.TongThuNhap) as LuongCaoNhat FROM PhongBan pb JOIN ChucVu cv ON pb.MaPhongBan = cv.MaPhongBan JOIN NhanVien nv ON cv.MaChucVu = nv.MaChucVu JOIN Luong l ON nv.MaNhanVien = l.MaNhanVien WHERE l.Thang = 5 AND l.Nam = 2025 GROUP BY pb.MaPhongBan, pb.TenPhongBan ORDER BY TongLuong DESC
            → TYPE: ADMIN_DEPARTMENT_REPORT

        16. "Nhân viên đi muộn nhiều nhất tháng 6"
            → SQL: SELECT TOP 10 nv.MaNhanVien, nv.HoTen, pb.TenPhongBan, COUNT(*) as SoLanMuon FROM NhanVien nv JOIN ChamCong cc ON nv.MaNhanVien = cc.MaNhanVien LEFT JOIN ChucVu cv ON nv.MaChucVu = cv.MaChucVu LEFT JOIN PhongBan pb ON cv.MaPhongBan = pb.MaPhongBan WHERE cc.TrangThai LIKE N'%%Muộn%%' AND MONTH(cc.Ngay) = 6 AND YEAR(cc.Ngay) = 2025 GROUP BY nv.MaNhanVien, nv.HoTen, pb.TenPhongBan ORDER BY SoLanMuon DESC
            → TYPE: ADMIN_ATTENDANCE_REPORT

        17. "Thống kê tổng quan công ty"
            → SQL: SELECT 'TongNhanVien' as Metric, COUNT(*) as Value FROM NhanVien WHERE TrangThaiLamViec = N'Đang làm việc' UNION ALL SELECT 'TongPhongBan', COUNT(*) FROM PhongBan UNION ALL SELECT 'LuongTrungBinh', CAST(AVG(TongThuNhap) as INT) FROM Luong WHERE Thang = 6 AND Nam = 2025 UNION ALL SELECT 'TongChiPhiLuong', CAST(SUM(TongThuNhap) as BIGINT) FROM Luong WHERE Thang = 6 AND Nam = 2025
            → TYPE: ADMIN_COMPANY_STATS

        18. "Phân tích chi phí nhân sự tháng 5/2025" hoặc "Tổng chi phí lương công ty"
            → SQL: SELECT SUM(l.TongThuNhap) as TongChiPhiLuong, AVG(l.TongThuNhap) as LuongTrungBinh, COUNT(DISTINCT l.MaNhanVien) as SoNhanVien, SUM(l.TienTangCa) as TongTienTangCa, SUM(l.ThueThuNhap) as TongThue FROM Luong l WHERE l.Thang = 5 AND l.Nam = 2025
            → TYPE: ADMIN_FINANCIAL_ANALYSIS
                        
        19. "Tổng lương toàn công ty tháng 4/2025"
            → SQL: SELECT SUM(l.TongThuNhap) as TongChiPhiLuong, AVG(l.TongThuNhap) as LuongTrungBinh, COUNT(DISTINCT l.MaNhanVien) as SoNhanVien, SUM(l.TienTangCa) as TongTienTangCa, SUM(l.ThueThuNhap) as TongThue FROM Luong l WHERE l.Thang = 4 AND l.Nam = 2025
            → TYPE: ADMIN_FINANCIAL_ANALYSIS

        20. "Các hợp đồng sắp hết hạn" hoặc "Hợp đồng sắp hết hạn"
            → SQL: SELECT hd.MaHopDong, hd.MaNhanVien, nv.HoTen, hd.LoaiHopDong, hd.NgayBatDau, hd.NgayKetThuc, hd.LuongCoBan, DATEDIFF(day, GETDATE(), hd.NgayKetThuc) as SoNgayConLai FROM HopDong hd JOIN NhanVien nv ON hd.MaNhanVien = nv.MaNhanVien WHERE hd.NgayKetThuc >= GETDATE() AND hd.NgayKetThuc <= DATEADD(month, 3, GETDATE()) ORDER BY hd.NgayKetThuc ASC
            → TYPE: ADMIN_CONTRACT_REPORT
                        
        21. "Hợp đồng của nhân viên tên Nguyễn Minh Sáng"
            → SQL: SELECT hd.MaHopDong, hd.MaNhanVien, nv.HoTen, hd.LoaiHopDong, hd.NgayBatDau, hd.NgayKetThuc, hd.LuongCoBan FROM HopDong hd JOIN NhanVien nv ON hd.MaNhanVien = nv.MaNhanVien WHERE nv.HoTen LIKE N'%%Nguyễn Minh Sáng%%'
            → TYPE: ADMIN_CONTRACT_REPORT
                        
        22. "Danh sách tất cả hợp đồng"
            → SQL: SELECT hd.MaHopDong, hd.MaNhanVien, nv.HoTen, hd.LoaiHopDong, hd.NgayBatDau, hd.NgayKetThuc, hd.LuongCoBan FROM HopDong hd JOIN NhanVien nv ON hd.MaNhanVien = nv.MaNhanVien ORDER BY hd.NgayKetThuc DESC
            → TYPE: ADMIN_CONTRACT_REPORT
        
        ⚠️ QUAN TRỌNG - PHÂN LOẠI QueryType:
        - "của tôi" → MY_xxx (Personal queries)
        - "thông tin của nhân viên tên" → EMPLOYEE_SEARCH  
        - "nhân viên có mã/ID" → EMPLOYEE_SEARCH
        - "danh sách tất cả nhân viên" → ADMIN_ALL_EMPLOYEES
        - "nhân viên nào được thưởng nhiều nhất" → ADMIN_SALARY_ANALYSIS
        - "lịch sử thưởng năm" → ADMIN_SALARY_ANALYSIS
        - "tiền phạt tổng cộng" → ADMIN_FINANCIAL_ANALYSIS
        - "thống kê tăng ca theo phòng ban" → ADMIN_DEPARTMENT_REPORT
        - "nhân viên vi phạm nhiều nhất" → ADMIN_ATTENDANCE_REPORT
        - "hợp đồng" → ADMIN_CONTRACT_REPORT (contract queries)
        - "báo cáo lương theo phòng ban" → ADMIN_DEPARTMENT_REPORT
        - "đi muộn nhiều nhất" → ADMIN_ATTENDANCE_REPORT
        - "thống kê tổng quan" → ADMIN_COMPANY_STATS
        - "phân tích chi phí" hoặc "tổng chi phí" → ADMIN_FINANCIAL_ANALYSIS
        
        BẢO MẬT: %s
        
        YÊU CẦU:
        1. LUÔN dùng TOP thay vì LIMIT
        2. Với personal queries (MY_xxx): Thay USER_ID bằng MaNhanVien = %d
        3. Với admin queries: KHÔNG cần WHERE MaNhanVien (full access)
        4. Sử dụng LIKE N'%%text%%' cho tìm kiếm Unicode
        5. JOIN đầy đủ để lấy thông tin phòng ban, chức vụ
        6. ⚠️ SQL PHẢI TRÊN 1 DÒNG DUY NHẤT - KHÔNG có \\n hay newline
        7. SQL chỉ được có dấu cách để phân cách, không có ký tự xuống dòng
        8. Ưu tiên dữ liệu tháng 5/2025 thay vì tháng 6/2025 nếu không có yêu cầu cụ thể
        
        PHẢN HỒI JSON BẮT BUỘC:
        {
            "success": true,
            "sql": "SELECT...",
            "explanation": "Mô tả truy vấn",
            "query_type": "MY_SALARY",
            "parameters": [],
            "estimated_rows": 10
        }
        
        ❌ NẾU KHÔNG HIỂU: query_type = "UNKNOWN"
        ✅ PERSONAL: MY_LEAVE|MY_SALARY|MY_INFO|MY_ATTENDANCE|MY_CONTRACT|MY_BONUS|MY_VIOLATION
        ✅ ADMIN: EMPLOYEE_SEARCH|ADMIN_ALL_EMPLOYEES|ADMIN_SALARY_ANALYSIS|ADMIN_DEPARTMENT_REPORT|ADMIN_ATTENDANCE_REPORT|ADMIN_COMPANY_STATS|ADMIN_FINANCIAL_ANALYSIS|ADMIN_CONTRACT_REPORT
        ✅ SPECIAL: AI_GENERATED (for complex queries)
        """,
                userName,                          // 1. %s - Tên
                employeeId,                        // 2. %d - ID
                role,                             // 3. %s - Vai trò
                deptId != null ? deptId.toString() : "NULL", // 4. %s - Phòng ban ID
                schema,                           // 5. %s - SCHEMA
                query,                            // 6. %s - CÂU HỎI
                employeeId,                       // 7. %d - Personal examples
                employeeId,                       // 8. %d
                employeeId,                       // 9. %d
                employeeId,                       // 10. %d
                employeeId,                       // 11. %d
                employeeId,                       // 12. %d - Contract example
                securityContext,                  // 13. %s - BẢO MẬT
                employeeId                        // 14. %d - YÊU CẦU
        );
    }

    private QueryResult parseAIResponse(String aiResponse, String originalQuery, NhanVien currentUser, UserRole role) {
        try {
            String jsonResponse = extractJSON(aiResponse);

            if (jsonResponse.contains("\"success\": true") || jsonResponse.contains("\"success\":true")) {
                String sql = extractValue(jsonResponse, "sql");
                String explanation = extractValue(jsonResponse, "explanation");
                String queryTypeStr = extractValue(jsonResponse, "query_type");

                log.info("🤖 AI QueryType: '{}' for question: '{}'", queryTypeStr, originalQuery);

                QueryType finalQueryType = determineQueryType(queryTypeStr, sql, originalQuery);

                log.info("🔧 Final QueryType: '{}' (AI: '{}')", finalQueryType, queryTypeStr);

                // Validate security
                if (!isSecureQuery(sql, role, currentUser)) {
                    return QueryResult.builder()
                            .queryType(QueryType.PERMISSION_DENIED)
                            .originalQuestion(originalQuery)
                            .currentUser(currentUser.getHoTen())
                            .userRole(role)
                            .build();
                }

                return QueryResult.builder()
                        .queryType(finalQueryType)
                        .sqlQuery(sql)
                        .parameters(new ArrayList<>())
                        .originalQuestion(originalQuery)
                        .currentUser(currentUser.getHoTen())
                        .userRole(role)
                        .explanation(explanation)
                        .needsUserContext(false)
                        .build();
            } else {
                return QueryResult.builder()
                        .queryType(QueryType.UNKNOWN)
                        .originalQuestion(originalQuery)
                        .currentUser(currentUser.getHoTen())
                        .userRole(role)
                        .build();
            }

        } catch (Exception e) {
            log.error("❌ Parse error: {}", e.getMessage(), e);
            return QueryResult.builder()
                    .queryType(QueryType.UNKNOWN)
                    .originalQuestion(originalQuery)
                    .currentUser(currentUser.getHoTen())
                    .userRole(role)
                    .build();
        }
    }

    // ✅ ENHANCED: Better QueryType determination with specific patterns
    private QueryType determineQueryType(String queryTypeStr, String sql, String originalQuestion) {
        // First, try to use AI's QueryType if valid
        if (queryTypeStr != null && !queryTypeStr.isEmpty() && !"UNKNOWN".equalsIgnoreCase(queryTypeStr)) {
            try {
                QueryType aiQueryType = QueryType.valueOf(queryTypeStr.toUpperCase());

                // ✅ VALIDATION: Check if AI's choice makes sense
                String lowerQuestion = originalQuestion.toLowerCase();
                String lowerSQL = sql.toLowerCase();

                // Validate bonus vs violation confusion
                if (aiQueryType == QueryType.MY_VIOLATION &&
                        (lowerQuestion.contains("thưởng") || lowerQuestion.contains("bonus") ||
                                lowerSQL.contains("nhanvien_thuong") || lowerSQL.contains("join thuong"))) {
                    log.warn("⚠️ AI misclassified bonus query as violation, correcting to MY_BONUS");
                    return QueryType.MY_BONUS;
                }

                if (aiQueryType == QueryType.MY_BONUS &&
                        (lowerQuestion.contains("vi phạm") || lowerQuestion.contains("phạt") ||
                                lowerSQL.contains("nhanvien_vipham") || lowerSQL.contains("join vipham"))) {
                    log.warn("⚠️ AI misclassified violation query as bonus, correcting to MY_VIOLATION");
                    return QueryType.MY_VIOLATION;
                }

                return aiQueryType;
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Invalid QueryType from AI: '{}', falling back to detection", queryTypeStr);
            }
        }

        // ✅ CONTRACT KEYWORDS DETECTION - HIGHEST PRIORITY
        if (containsAnyIgnoreCase(originalQuestion, Arrays.asList(
                "hợp đồng", "hợp đồng sắp hết hạn", "contract", "hết hạn", "gia hạn"
        ))) {
            return QueryType.ADMIN_CONTRACT_REPORT;
        }

        // ✅ SPECIFIC PATTERNS from error logs - HIGH PRIORITY
        String lowerQuestion = originalQuestion.toLowerCase();
        String lowerSQL = sql.toLowerCase();

        // Financial analysis patterns
        if (lowerQuestion.contains("tiền phạt tổng cộng") || lowerQuestion.contains("tổng tiền phạt") ||
                lowerSQL.contains("sum(vp.sotienphat)") || lowerSQL.contains("tongtienPhat")) {
            return QueryType.ADMIN_FINANCIAL_ANALYSIS;
        }

        // Salary analysis patterns
        if ((lowerQuestion.contains("lịch sử thưởng") && lowerQuestion.contains("năm")) ||
                (lowerQuestion.contains("nhân viên") && lowerQuestion.contains("thưởng") && lowerQuestion.contains("nhiều nhất")) ||
                lowerSQL.contains("sum(nt.muctien)") || lowerSQL.contains("tongtienthuong")) {
            return QueryType.ADMIN_SALARY_ANALYSIS;
        }

        // Department report patterns
        if (lowerQuestion.contains("thống kê tăng ca") || (lowerQuestion.contains("tăng ca") && lowerQuestion.contains("phòng ban")) ||
                lowerSQL.contains("sum(cc.sogiotangca)") || lowerSQL.contains("tongsogioteangca")) {
            return QueryType.ADMIN_DEPARTMENT_REPORT;
        }

        // Attendance report patterns
        if ((lowerQuestion.contains("nhân viên") && lowerQuestion.contains("vi phạm") && lowerQuestion.contains("nhiều nhất")) ||
                (lowerSQL.contains("count(*) as solanvipham") || (lowerSQL.contains("nhanvien_vipham") && lowerSQL.contains("group by")))) {
            return QueryType.ADMIN_ATTENDANCE_REPORT;
        }

        // Standard admin queries detection
        if (lowerQuestion.contains("thông tin") && lowerQuestion.contains("nhân viên") &&
                (lowerQuestion.contains("tên") || lowerQuestion.contains("mã"))) {
            return QueryType.EMPLOYEE_SEARCH;
        }
        if (lowerQuestion.contains("danh sách") && lowerQuestion.contains("tất cả")) {
            return QueryType.ADMIN_ALL_EMPLOYEES;
        }
        if (lowerQuestion.contains("top") && lowerQuestion.contains("lương")) {
            return QueryType.ADMIN_SALARY_ANALYSIS;
        }
        if (lowerQuestion.contains("lương trung bình")) {
            return QueryType.ADMIN_SALARY_ANALYSIS;
        }
        if (lowerQuestion.contains("báo cáo lương")) {
            return QueryType.ADMIN_DEPARTMENT_REPORT;
        }
        if (lowerQuestion.contains("đi muộn") || (lowerQuestion.contains("muộn") && lowerQuestion.contains("nhất"))) {
            return QueryType.ADMIN_ATTENDANCE_REPORT;
        }
        if (lowerQuestion.contains("thống kê tổng quan")) {
            return QueryType.ADMIN_COMPANY_STATS;
        }
        if (lowerQuestion.contains("phân tích chi phí") || lowerQuestion.contains("tổng chi phí")) {
            return QueryType.ADMIN_FINANCIAL_ANALYSIS;
        }

        // Personal queries detection
        if (lowerQuestion.contains("của tôi")) {
            if (lowerQuestion.contains("lương")) return QueryType.MY_SALARY;
            if (lowerQuestion.contains("thưởng")) return QueryType.MY_BONUS;
            if (lowerQuestion.contains("vi phạm")) return QueryType.MY_VIOLATION;
            if (lowerQuestion.contains("chấm công")) return QueryType.MY_ATTENDANCE;
            if (lowerQuestion.contains("nghỉ phép")) return QueryType.MY_LEAVE;
            if (lowerQuestion.contains("hợp đồng")) return QueryType.MY_CONTRACT;
            if (lowerQuestion.contains("thông tin")) return QueryType.MY_INFO;
        }

        // SQL-based detection as fallback
        if (lowerSQL.contains("where hoten like") || (lowerSQL.contains("where manhanvien =") && !lowerSQL.contains("1006"))) {
            return QueryType.EMPLOYEE_SEARCH;
        }
        if (lowerSQL.contains("group by") && lowerSQL.contains("phongban")) {
            return QueryType.ADMIN_DEPARTMENT_REPORT;
        }
        if (lowerSQL.contains("order by") && lowerSQL.contains("desc") && lowerSQL.contains("luong")) {
            return QueryType.ADMIN_SALARY_ANALYSIS;
        }

        // Employee personal queries
        if (lowerSQL.contains("from luong") || lowerSQL.contains("join luong")) {
            return QueryType.MY_SALARY;
        }
        if (lowerSQL.contains("from chamcong") || lowerSQL.contains("join chamcong")) {
            return QueryType.MY_ATTENDANCE;
        }
        if (lowerSQL.contains("from nghiphep") || lowerSQL.contains("join nghiphep")) {
            return QueryType.MY_LEAVE;
        }
        if (lowerSQL.contains("from hopdong") || lowerSQL.contains("join hopdong")) {
            return QueryType.MY_CONTRACT;
        }
        if (lowerSQL.contains("from nhanvien_thuong") || lowerSQL.contains("join thuong")) {
            return QueryType.MY_BONUS;
        }
        if (lowerSQL.contains("from nhanvien_vipham") || lowerSQL.contains("join vipham")) {
            return QueryType.MY_VIOLATION;
        }

        // Employee info detection
        if (lowerSQL.contains("from nhanvien") && !lowerSQL.contains("join")) {
            if (lowerSQL.contains("count") || lowerSQL.contains("group by")) {
                return QueryType.TEAM_INFO;
            }
            return lowerSQL.contains("where manhanvien =") ? QueryType.MY_INFO : QueryType.ADMIN_ALL_EMPLOYEES;
        }

        return QueryType.AI_GENERATED;
    }

    private boolean isSecureQuery(String sql, UserRole role, NhanVien currentUser) {
        String lowerSQL = sql.toLowerCase();

        // Block dangerous operations
        if (lowerSQL.contains("drop ") || lowerSQL.contains("delete ") ||
                lowerSQL.contains("update ") || lowerSQL.contains("insert ") ||
                lowerSQL.contains("truncate ") || lowerSQL.contains("alter ")) {
            log.warn("🚫 Blocked dangerous SQL operation: {}", sql);
            return false;
        }

        Integer employeeId = currentUser.getId();
        Integer deptId = permissionService.getCurrentDepartmentId();

        // Role-specific validations
        switch (role) {
            case EMPLOYEE:
                if (!needsEmployeeFilter(sql, employeeId, role.toString())) {
                    return true; // Already has filter
                }
                log.warn("🚫 Employee query missing user filter: {}", sql);
                return false;

            case MANAGER:
                boolean hasDeptFilter = deptId != null &&
                        (lowerSQL.contains("maphongban = " + deptId) || lowerSQL.contains("maphongban=" + deptId));
                boolean hasOwnFilter = lowerSQL.contains("manhanvien = " + employeeId) ||
                        lowerSQL.contains("manhanvien=" + employeeId);

                if (!hasDeptFilter && !hasOwnFilter) {
                    log.warn("🚫 Manager query missing department/user filter: {}", sql);
                    return false;
                }
                return true;

            case HR:
            case ADMIN:
                // ✅ ADMIN has full access - no restrictions
                return true;

            default:
                return false;
        }
    }

    private String getDatabaseSchemaForRole(UserRole role) {
        return switch (role) {
            case EMPLOYEE -> getEmployeeSchema();
            case MANAGER -> getManagerSchema();
            case HR -> getHRSchema();
            case ADMIN -> getAdminSchema();
            default -> "KHÔNG CÓ QUYỀN TRUY CẬP";
        };
    }

    // ✅ ENHANCED: Admin schema with updated examples
    private String getAdminSchema() {
        return """
            BẢNG ĐẦY ĐỦ CHO ADMIN (Full access - no restrictions):
            
            1. NhanVien - Thông tin nhân viên (TẤT CẢ)
               Columns: MaNhanVien, HoTen, GioiTinh, NgaySinh, SDT, Email, DiaChi, 
                       TrinhDoHocVan, SoNgayPhep, SoNgayPhepDaSuDung, ThamNien, 
                       LuongHienTai, TrangThaiLamViec, MaChucVu
            
            2. ChucVu - Chức vụ
               Columns: MaChucVu, TenChucVu, MaQuyen, MaPhongBan
               
            3. PhongBan - Phòng ban  
               Columns: MaPhongBan, TenPhongBan
               
            4. Luong - Bảng lương (TẤT CẢ nhân viên)
               Columns: MaBangLuong, MaNhanVien, Thang, Nam, TienTangCa, SoNguoiPhuThuoc, 
                       ThueThuNhap, TongThuNhap, NgayNhan
            
            5. ChamCong - Chấm công (TẤT CẢ nhân viên)
               Columns: MaChamCong, MaNhanVien, Ngay, GioVao, GioRa, TrangThai, SoGioTangCa
            
            6. NghiPhep - Nghỉ phép (TẤT CẢ nhân viên)
               Columns: MaNghiPhep, MaNhanVien, NgayBatDau, NgayKetThuc, LyDo, TrangThaiPheDuyet
            
            7. HopDong - Hợp đồng (TẤT CẢ nhân viên)
               Columns: MaHopDong, MaNhanVien, LoaiHopDong, NgayBatDau, NgayKetThuc, 
                       LuongCoBan, TrangThai
            
            8. NhanVien_Thuong - Thưởng (TẤT CẢ nhân viên)
               Columns: MaNhanVien, MaThuong, NgayThuong, MucTien
               JOIN: Thuong ON MaThuong (TenThuong, MucThuong, NguoiRaQuyetDinh)
            
            9. NhanVien_ViPham - Vi phạm (TẤT CẢ nhân viên)  
               Columns: MaNhanVien, MaViPham, NgayViPham, MoTa, NguoiRaQuyetDinh
               JOIN: ViPham ON MaViPham (LoaiViPham, HinhThucPhat, SoTienPhat)
            
            10. Quyen - Quyền hạn
                Columns: MaQuyen, TenQuyen, MoTa
            
            ⚠️ SPECIFIC PATTERNS for common queries:
            - Tổng tiền phạt: SELECT SUM(vp.SoTienPhat) AS TongTienPhat FROM NhanVien_ViPham nv JOIN ViPham vp ON nv.MaViPham = vp.MaViPham
            - Lịch sử thưởng: SELECT nv.*, t.TenThuong, t.MucThuong FROM NhanVien_Thuong nv JOIN Thuong t ON nv.MaThuong = t.MaThuong WHERE YEAR(nv.NgayThuong) = 2025
            - Thống kê tăng ca: SELECT pb.TenPhongBan, SUM(cc.SoGioTangCa) AS TongSoGioTangCa FROM PhongBan pb JOIN ChucVu cv ON pb.MaPhongBan = cv.MaPhongBan JOIN NhanVien nv ON cv.MaChucVu = nv.MaChucVu JOIN ChamCong cc ON nv.MaNhanVien = cc.MaNhanVien GROUP BY pb.MaPhongBan, pb.TenPhongBan
            - Hợp đồng sắp hết hạn: SELECT hd.*, nv.HoTen, DATEDIFF(day, GETDATE(), hd.NgayKetThuc) as SoNgayConLai FROM HopDong hd JOIN NhanVien nv ON hd.MaNhanVien = nv.MaNhanVien WHERE hd.NgayKetThuc >= GETDATE() AND hd.NgayKetThuc <= DATEADD(month, 3, GETDATE())
            - Vi phạm nhiều nhất: SELECT TOP 10 nv.MaNhanVien, nv.HoTen, pb.TenPhongBan, COUNT(*) AS SoLanViPham FROM NhanVien nv JOIN NhanVien_ViPham nvp ON nv.MaNhanVien = nvp.MaNhanVien LEFT JOIN ChucVu cv ON nv.MaChucVu = cv.MaChucVu LEFT JOIN PhongBan pb ON cv.MaPhongBan = pb.MaPhongBan GROUP BY nv.MaNhanVien, nv.HoTen, pb.TenPhongBan ORDER BY SoLanViPham DESC
            
            BẢO MẬT: ADMIN có FULL ACCESS - không cần giới hạn WHERE cho personal data
            ⚠️ CẤM TUYỆT ĐỐI: SQL PHẢI VIẾT TRÊN 1 DÒNG - KHÔNG có newline
            ⚠️ ƯU TIÊN: Dữ liệu tháng 5/2025 thay vì tháng 6/2025 nếu không có yêu cầu cụ thể
            """;
    }

    private String getEmployeeSchema() {
        return """
            BẢNG CHO NHÂN VIÊN (CHỈ XEM THÔNG TIN CỦA MÌNH):
            
            1. NhanVien - Thông tin cá nhân
               Columns: MaNhanVien, HoTen, GioiTinh, NgaySinh, SDT, Email, DiaChi, 
                       TrinhDoHocVan, SoNgayPhep, SoNgayPhepDaSuDung, ThamNien, LuongHienTai, TrangThaiLamViec, MaChucVu
            
            2. Luong - Bảng lương  
               Columns: MaBangLuong, MaNhanVien, Thang, Nam, TienTangCa, SoNguoiPhuThuoc, 
                       ThueThuNhap, TongThuNhap, NgayNhan
            
            3. ChamCong - Chấm công
               Columns: MaChamCong, MaNhanVien, Ngay, GioVao, GioRa, TrangThai, SoGioTangCa
            
            4. NghiPhep - Nghỉ phép
               Columns: MaNghiPhep, MaNhanVien, NgayBatDau, NgayKetThuc, LyDo, TrangThaiPheDuyet
            
            5. HopDong - Hợp đồng
               Columns: MaHopDong, MaNhanVien, LoaiHopDong, NgayBatDau, NgayKetThuc, 
                       LuongCoBan, TrangThai
            
            6. NhanVien_Thuong - Thưởng nhân viên
               Columns: MaNhanVien, MaThuong, NgayThuong, MucTien  
               JOIN: Thuong ON MaThuong (TenThuong, MucThuong, NguoiRaQuyetDinh)
            
            7. NhanVien_ViPham - Vi phạm nhân viên
               Columns: MaNhanVien, MaViPham, NgayViPham, MoTa, NguoiRaQuyetDinh, NgayRaQuyetDinh
               JOIN: ViPham ON MaViPham (LoaiViPham, HinhThucPhat, SoTienPhat)
            
            BẢO MẬT: CHỈ được WHERE MaNhanVien = USER_ID
            """;
    }

    private String getManagerSchema() {
        return getEmployeeSchema() + """
            
            BẢNG THÊM CHO MANAGER (Xem team + cá nhân):
            
            8. PhongBan - Phòng ban
               Columns: MaPhongBan, TenPhongBan
            
            9. ChucVu - Chức vụ  
               Columns: MaChucVu, TenChucVu, MaQuyen, MaPhongBan
            
            BẢO MẬT: WHERE MaPhongBan = DEPT_ID OR MaNhanVien = USER_ID
            """;
    }

    private String getHRSchema() {
        return getManagerSchema() + """
            
            BẢNG THÊM CHO HR (Toàn quyền nhân sự):
            
            12. Thuong - Danh mục thưởng
            13. PhuCap - Danh mục phụ cấp
            14. ViPham - Danh mục vi phạm
            15. BaoHiem - Bảo hiểm
            
            BẢO MẬT: Có thể xem TẤT CẢ dữ liệu nhân viên
            """;
    }

    // ✅ HELPER METHODS - Fixed and added missing methods
    private String extractJSON(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}") + 1;
        return (start >= 0 && end > start) ? response.substring(start, end) : response;
    }

    private String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            return m.find() ? m.group(1) : "";
        } catch (Exception e) {
            return "";
        }
    }

    // ✅ ADDED MISSING HELPER METHODS
    private boolean containsAnyIgnoreCase(String text, java.util.List<String> keywords) {
        if (text == null || keywords == null) return false;
        String lowerText = text.toLowerCase();
        return keywords.stream().anyMatch(keyword -> lowerText.contains(keyword.toLowerCase()));
    }

    private boolean needsEmployeeFilter(String sql, int employeeId, String userRole) {
        String lowerSQL = sql.toLowerCase();

        // Check if employee filter already exists
        if (lowerSQL.contains("manhanvien = " + employeeId)) {
            return false; // Already filtered
        }

        // Only non-admin roles need filtering
        return !"ADMIN".equalsIgnoreCase(userRole);
    }
}
package com.example.hrm.ai;

import com.example.hrm.domain.NhanVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@Service
public class HRMDatabaseExecutor {

    private static final Logger log = LoggerFactory.getLogger(HRMDatabaseExecutor.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HRMPermissionService permissionService;

    public DatabaseResponse executeQuery(QueryResult queryResult) {
        try {
            // Handle permission denied
            if (queryResult.getQueryType() == QueryType.PERMISSION_DENIED) {
                return DatabaseResponse.builder()
                        .success(false)
                        .error("Bạn không có quyền truy cập thông tin này.")
                        .queryType(queryResult.getQueryType())
                        .originalQuestion(queryResult.getOriginalQuestion())
                        .build();
            }

            // Handle unknown queries
            if (queryResult.getQueryType() == QueryType.UNKNOWN) {
                return DatabaseResponse.builder()
                        .success(false)
                        .error("Không thể hiểu câu hỏi của bạn. Vui lòng hỏi rõ hơn.")
                        .queryType(queryResult.getQueryType())
                        .originalQuestion(queryResult.getOriginalQuestion())
                        .build();
            }

            String sql = queryResult.getSqlQuery();
            List<Object> params = queryResult.getParameters();

            // ✅ AUTO-FIX SQL SERVER SYNTAX - CONVERT LIMIT TO TOP
            sql = fixSQLServerSyntax(sql);

            // ✅ NEW: Enhanced logging for different query types
            UserRole currentRole = permissionService.getCurrentUserRole();
            String currentUser = permissionService.getCurrentUserName();

            log.info("🗃️ Executing SQL: {}", sql);
            log.info("📝 Parameters: {}", params);
            log.info("👤 User: {} | Role: {} | QueryType: {}",
                    currentUser, currentRole, queryResult.getQueryType());

            // ✅ CRITICAL FIX: Clean SQL - Remove newlines and normalize whitespace
            String cleanedSQL = cleanSQLForExecution(sql);
            log.info("🧹 Cleaned SQL: {}", cleanedSQL);

            // ✅ IMPROVED: Role-specific validation
            if (!isValidSQL(cleanedSQL, queryResult.getQueryType(), currentRole)) {
                return DatabaseResponse.builder()
                        .success(false)
                        .error("Câu truy vấn không hợp lệ hoặc không an toàn.")
                        .queryType(queryResult.getQueryType())
                        .originalQuestion(queryResult.getOriginalQuestion())
                        .build();
            }

            // ✅ NEW: Security check based on role and query type
            if (!isQueryAuthorized(cleanedSQL, queryResult.getQueryType(), currentRole)) {
                return DatabaseResponse.builder()
                        .success(false)
                        .error("Bạn không có quyền thực hiện truy vấn này.")
                        .queryType(queryResult.getQueryType())
                        .originalQuestion(queryResult.getOriginalQuestion())
                        .build();
            }

            List<Map<String, Object>> results;

            // Execute query with cleaned SQL
            if (params != null && !params.isEmpty()) {
                results = jdbcTemplate.queryForList(cleanedSQL, params.toArray());
            } else {
                results = jdbcTemplate.queryForList(cleanedSQL);
            }

            log.info("📊 Query results: {} rows", results.size());

            // ✅ IMPROVED: Enhanced audit logging
            logQueryAudit(cleanedSQL, results.size(), queryResult.getQueryType(), currentRole);

            return DatabaseResponse.builder()
                    .success(true)
                    .data(results)
                    .queryType(queryResult.getQueryType())
                    .originalQuestion(queryResult.getOriginalQuestion())
                    .totalRows(results.size())
                    .build();

        } catch (Exception e) {
            log.error("❌ SQL Execution Error: {}", e.getMessage(), e);

            // ✅ IMPROVED: Enhanced error logging
            logQueryError(queryResult.getSqlQuery(), e.getMessage(), queryResult.getQueryType());

            return DatabaseResponse.builder()
                    .success(false)
                    .error("Lỗi khi truy vấn dữ liệu: " + getFriendlyErrorMessage(e))
                    .queryType(queryResult.getQueryType())
                    .originalQuestion(queryResult.getOriginalQuestion())
                    .build();
        }
    }

    // ✅ NEW: SQL Cleaning method to fix newline issues
    private String cleanSQLForExecution(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        try {
            // Remove all types of newlines and normalize whitespace
            String cleaned = sql.replaceAll("\\r\\n", " ")  // Windows newlines
                    .replaceAll("\\n", " ")      // Unix newlines
                    .replaceAll("\\r", " ")      // Mac newlines
                    .replaceAll("\\s+", " ")     // Multiple spaces to single space
                    .trim();                     // Remove leading/trailing spaces

            log.debug("🧹 SQL cleaned: '{}' → '{}'", sql.replaceAll("\\s+", " "), cleaned);
            return cleaned;

        } catch (Exception e) {
            log.warn("⚠️ Failed to clean SQL, using original: {}", e.getMessage());
            return sql;
        }
    }

    // ✅ SIMPLE FIX: Just handle LIMIT and basic cases
    private String fixSQLServerSyntax(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        String originalSql = sql;
        String fixed = sql.trim();

        // Fix LIMIT → TOP
        if (fixed.toLowerCase().contains(" limit ")) {
            try {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "\\s+limit\\s+(\\d+)\\s*;?\\s*$",
                        java.util.regex.Pattern.CASE_INSENSITIVE
                );
                java.util.regex.Matcher matcher = pattern.matcher(fixed);

                if (matcher.find()) {
                    String limitNumber = matcher.group(1);
                    fixed = fixed.replaceFirst("(?i)^\\s*select", "SELECT TOP " + limitNumber);
                    fixed = matcher.replaceAll("");

                    log.info("🔧 LIMIT {} → TOP {} fixed", limitNumber, limitNumber);
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to fix LIMIT: {}", e.getMessage());
                return originalSql;
            }
        }

        // Basic SQL Server syntax fixes
        fixed = fixed.replaceAll("(?i)\\bNOW\\(\\)", "GETDATE()");
        fixed = fixed.replaceAll("(?i)\\bCURDATE\\(\\)", "CAST(GETDATE() AS DATE)");

        // ✅ DON'T auto-fix Unicode - let AI handle it correctly
        // AI already generates N'...' correctly, our regex was breaking it

        return fixed;
    }

    // ✅ IMPROVED: Role-based SQL validation
    private boolean isValidSQL(String sql, QueryType queryType, UserRole role) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        String lowerSQL = sql.toLowerCase().trim();

        // Must start with SELECT
        if (!lowerSQL.startsWith("select")) {
            log.warn("🚫 Non-SELECT query blocked: {}", sql);
            return false;
        }

        // Block LIMIT after auto-fix (should not happen)
        if (lowerSQL.contains(" limit ")) {
            log.warn("🚫 LIMIT syntax still present after auto-fix: {}", sql);
            return false;
        }

        // ✅ ROLE-SPECIFIC: Different validation for different roles
        if (role == UserRole.ADMIN) {
            // ADMIN has more relaxed validation but still safe
            return isAdminQueryValid(lowerSQL, queryType);
        } else {
            // Stricter validation for non-admin users
            return isStandardQueryValid(lowerSQL, queryType);
        }
    }

    // ✅ NEW: Admin-specific query validation
    private boolean isAdminQueryValid(String lowerSQL, QueryType queryType) {
        // Block dangerous operations even for ADMIN
        String[] dangerousKeywords = {
                "drop", "delete", "update", "insert", "truncate", "alter",
                "create", "exec", "execute", "sp_", "xp_", "bulk", "shutdown",
                "waitfor", "delay"
        };

        for (String keyword : dangerousKeywords) {
            if (lowerSQL.contains(" " + keyword + " ") ||
                    lowerSQL.contains(" " + keyword + "(") ||
                    lowerSQL.endsWith(" " + keyword)) {
                log.warn("🚫 Dangerous keyword '{}' blocked even for ADMIN: {}", keyword, lowerSQL);
                return false;
            }
        }

        // ADMIN can access all tables and use complex queries
        return true;
    }

    // ✅ IMPROVED: Standard user query validation
    private boolean isStandardQueryValid(String lowerSQL, QueryType queryType) {
        // Block dangerous keywords
        String[] dangerousKeywords = {
                "drop", "delete", "update", "insert", "truncate", "alter",
                "create", "exec", "execute", "sp_", "xp_", "bulk", "shutdown",
                "waitfor", "delay"
        };

        for (String keyword : dangerousKeywords) {
            if (lowerSQL.contains(" " + keyword + " ") ||
                    lowerSQL.contains(" " + keyword + "(") ||
                    lowerSQL.endsWith(" " + keyword)) {
                log.warn("🚫 Dangerous keyword '{}' found in SQL: {}", keyword, lowerSQL);
                return false;
            }
        }

        // Check for SQL injection patterns
        String[] injectionPatterns = {
                "--", "/*", "*/", "@@", "char(", "nchar(", "varchar(", "nvarchar(",
                "benchmark", "sleep(", "pg_sleep"
        };

        for (String pattern : injectionPatterns) {
            if (lowerSQL.contains(pattern)) {
                log.warn("🚫 Potential SQL injection pattern '{}' found: {}", pattern, lowerSQL);
                return false;
            }
        }

        return true;
    }

    // ✅ NEW: Authorization check based on role and query type
    private boolean isQueryAuthorized(String sql, QueryType queryType, UserRole role) {
        // ADMIN can execute any valid query
        if (role == UserRole.ADMIN) {
            log.info("🔑 ADMIN access granted for QueryType: {}", queryType);
            return true;
        }

        // Check if role can access this query type
        if (!permissionService.canAccessQuery(queryType)) {
            log.warn("🚫 Role {} cannot access QueryType: {}", role, queryType);
            return false;
        }

        // For personal queries, ensure user filter is present
        if (queryType.name().startsWith("MY_")) {
            Integer currentUserId = permissionService.getCurrentEmployeeId();
            if (currentUserId != null) {
                String lowerSQL = sql.toLowerCase();
                boolean hasUserFilter = lowerSQL.contains("manhanvien = " + currentUserId) ||
                        lowerSQL.contains("manhanvien=" + currentUserId);

                if (!hasUserFilter) {
                    log.warn("🚫 Personal query missing user filter for ID: {}", currentUserId);
                    return false;
                }
            }
        }

        return true;
    }

    // ✅ IMPROVED: Better error messages for SQL syntax issues
    private String getFriendlyErrorMessage(Exception e) {
        String message = e.getMessage().toLowerCase();

        if (message.contains("syntax error") || message.contains("incorrect syntax")) {
            if (message.contains("\\")) {
                return "Lỗi cú pháp SQL - ký tự newline đã được làm sạch tự động.";
            }
            return "Cú pháp truy vấn không chính xác. Đã tự động sửa lỗi LIMIT thành TOP.";
        } else if (message.contains("invalid column") || message.contains("invalid object")) {
            return "Tên cột hoặc bảng không tồn tại. Vui lòng kiểm tra lại cấu trúc database.";
        } else if (message.contains("permission denied") || message.contains("access denied")) {
            return "Không có quyền truy cập dữ liệu này.";
        } else if (message.contains("timeout")) {
            return "Truy vấn mất quá nhiều thời gian. Vui lòng thử câu hỏi đơn giản hơn.";
        } else if (message.contains("limit")) {
            return "Lỗi cú pháp SQL Server - đã tự động chuyển LIMIT thành TOP.";
        } else if (message.contains("connection")) {
            return "Lỗi kết nối database. Vui lòng thử lại sau.";
        } else if (message.contains("maphongban")) {
            return "Cột MaPhongBan không tồn tại. Cần JOIN với bảng ChucVu để lấy thông tin phòng ban.";
        } else {
            return "Lỗi hệ thống. Vui lòng liên hệ IT support.";
        }
    }

    // ✅ ENHANCED: Better audit logging
    private void logQueryAudit(String sql, int resultCount, QueryType queryType, UserRole role) {
        try {
            String currentUser = permissionService.getCurrentUserName();
            Integer employeeId = permissionService.getCurrentEmployeeId();

            log.info("📝 AUDIT LOG - User: {} (ID: {}) | Role: {} | QueryType: {} | Results: {} | SQL: {}",
                    currentUser, employeeId, role, queryType, resultCount, sql);

            // ✅ SPECIAL: Log admin queries for security monitoring
            if (role == UserRole.ADMIN) {
                log.info("🔐 ADMIN AUDIT - {} executed {} returning {} rows",
                        currentUser, queryType, resultCount);
            }

            // TODO: Save audit log to database
            // insertAuditLog(employeeId, sql, queryType, resultCount, true, null, role);

        } catch (Exception e) {
            log.warn("⚠️ Failed to log audit: {}", e.getMessage());
        }
    }

    // ✅ IMPROVED: Enhanced error logging
    private void logQueryError(String sql, String error, QueryType queryType) {
        try {
            String currentUser = permissionService.getCurrentUserName();
            Integer employeeId = permissionService.getCurrentEmployeeId();
            UserRole role = permissionService.getCurrentUserRole();

            log.error("❌ ERROR LOG - User: {} (ID: {}) | Role: {} | QueryType: {} | Error: {} | SQL: {}",
                    currentUser, employeeId, role, queryType, error, sql);

            // ✅ ENHANCED: Categorize errors for better debugging
            if (error.toLowerCase().contains("invalid column")) {
                log.error("🔧 SCHEMA ERROR - Possible missing column or incorrect table structure");
            } else if (error.toLowerCase().contains("syntax")) {
                log.error("🔧 SYNTAX ERROR - SQL syntax needs fixing");
            } else if (error.toLowerCase().contains("\\")) {
                log.error("🔧 NEWLINE ERROR - SQL contains problematic newline characters");
            } else if (error.toLowerCase().contains("permission")) {
                log.error("🔧 PERMISSION ERROR - Access denied");
            }

            // TODO: Log errors to database for monitoring
            // insertAuditLog(employeeId, sql, queryType, 0, false, error, role);

        } catch (Exception e) {
            log.warn("⚠️ Failed to log error: {}", e.getMessage());
        }
    }

    // ✅ ENHANCED: Better testing utility
    public String testSQLFix(String originalSQL) {
        log.info("🧪 Testing SQL fix for: {}", originalSQL);
        String fixed = fixSQLServerSyntax(originalSQL);
        String cleaned = cleanSQLForExecution(fixed);
        log.info("🧪 Result: {}", cleaned);

        // Test validation too
        UserRole testRole = permissionService.getCurrentUserRole();
        boolean isValid = isValidSQL(cleaned, QueryType.AI_GENERATED, testRole);
        log.info("🧪 Validation: {}", isValid ? "✅ VALID" : "❌ INVALID");

        return cleaned;
    }

    // ✅ NEW: Method for testing admin queries
    public DatabaseResponse testAdminQuery(String sql) {
        if (!permissionService.isAdmin()) {
            return DatabaseResponse.builder()
                    .success(false)
                    .error("Only ADMIN can use this test method")
                    .build();
        }

        log.info("🧪 ADMIN TEST QUERY: {}", sql);

        try {
            String fixed = fixSQLServerSyntax(sql);
            String cleaned = cleanSQLForExecution(fixed);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(cleaned);

            log.info("🧪 ADMIN TEST RESULT: {} rows", results.size());

            return DatabaseResponse.builder()
                    .success(true)
                    .data(results)
                    .queryType(QueryType.AI_GENERATED)
                    .totalRows(results.size())
                    .build();

        } catch (Exception e) {
            log.error("🧪 ADMIN TEST ERROR: {}", e.getMessage());
            return DatabaseResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
}
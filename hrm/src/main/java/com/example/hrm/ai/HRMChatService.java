package com.example.hrm.ai;

import com.example.hrm.domain.NhanVien;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class HRMChatService {

    private static final Logger log = LoggerFactory.getLogger(HRMChatService.class);

    @Autowired
    private ChatService generalChatService;

    @Autowired
    private HRMAIQueryTranslator hrmTranslator;

    @Autowired
    private HRMDatabaseExecutor databaseExecutor;

    @Autowired
    private HRMResponseFormatter responseFormatter;

    @Autowired
    private HRMPermissionService permissionService;

    public String processMessage(String userMessage, String username) {
        log.info("🚀 === HRM CHAT SERVICE START ===");
        log.info("🚀 Message: '{}'", userMessage);
        log.info("🚀 Username: '{}'", username);

        try {
            // Get current user info
            NhanVien currentUser = permissionService.getCurrentUser();
            UserRole userRole = permissionService.getCurrentUserRole();

            log.info("👤 === USER INFO ===");
            if (currentUser != null) {
                log.info("👤 Name: {}", currentUser.getHoTen());
                log.info("👤 ID: {}", currentUser.getId());
                log.info("👤 Role: {}", userRole);
            } else {
                log.error("👤 ❌ Current user is NULL!");
                return "❌ Không thể xác định danh tính của bạn. Vui lòng đăng nhập lại.";
            }

            String cleanMessage = userMessage.trim();

            // Test HRM detection
            boolean isHRMDetected = isHRMQuestion(cleanMessage);
            log.info("🏢 HRM Detection Result: {}", isHRMDetected);

            if (isHRMDetected) {
                log.info("🏢 Routing to HRM system...");
                String result = processHRMQuestion(cleanMessage, currentUser, userRole);
                log.info("🏢 HRM Result length: {}", result.length());

                if (result.length() > 0) {
                    String preview = result.substring(0, Math.min(result.length(), 100));
                    log.info("🏢 HRM Result preview: {}", preview);
                }

                return result;
            } else {
                log.info("💬 Routing to General AI...");
                return processGeneralQuestion(cleanMessage, username);
            }

        } catch (Exception e) {
            log.error("❌ ProcessMessage error: {}", e.getMessage(), e);
            return "❌ Lỗi xử lý tin nhắn: " + e.getMessage();
        }
    }

    private boolean isHRMQuestion(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        String lower = message.toLowerCase().trim();

        String[] hrmKeywords = {
                // Personal HR Keywords
                "lương", "tiền lương", "thu nhập", "tăng ca", "overtime",
                "chấm công", "công", "đi làm", "nghỉ làm", "attendance",
                "nghỉ phép", "xin nghỉ", "phép năm", "leave", "vacation",
                "hợp đồng", "contract", "ký hợp đồng",
                "nhân viên", "employee", "đồng nghiệp", "staff", "colleague",
                "team", "nhóm", "đội", "phòng ban", "department",
                "thưởng", "bonus", "khen thưởng", "thưởng tết", "incentive",
                "thưởng tháng", "thưởng năm", "thưởng dự án", "thưởng hiệu suất",
                "tiền thưởng", "mức thưởng", "lịch sử thưởng",
                "vi phạm", "violation", "phạt", "kỷ luật", "discipline",
                "tiền phạt", "mức phạt", "hình thức phạt", "penalty", "fine",
                "lỗi", "sai phạm", "rule violation", "misconduct", "warning",
                "cảnh cáo", "khiển trách", "kỷ luật lao động",
                "phụ cấp", "allowance", "trợ cấp", "phụ cấp ăn trưa",
                "bảo hiểm", "insurance", "bhxh", "bhyt", "social insurance",
                "dự án", "project", "nhiệm vụ", "công việc", "task",
                "chức vụ", "position", "cấp bậc", "thăng chức", "promotion",
                "hr", "nhân sự", "phòng nhân sự", "human resource",
                "của tôi", "thông tin tôi", "tôi có", "tôi được", "my info",
                "salary", "wage", "income", "payroll", "compensation",
                "check in", "check out", "time tracking", "timesheet",
                "my salary", "my team", "my data", "my profile",

                // ✅ NEW: Admin Management Keywords
                "danh sách", "tất cả", "toàn bộ", "all employees", "list",
                "top", "cao nhất", "thấp nhất", "nhiều nhất", "ít nhất", "highest", "lowest",
                "báo cáo", "report", "thống kê", "statistics", "analytics", "dashboard",
                "phòng ban", "department", "theo phòng", "by department", "departmental",
                "công ty", "company", "toàn công ty", "company-wide", "organization",
                "phân tích", "analysis", "so sánh", "compare", "comparison", "trend",
                "tổng", "total", "trung bình", "average", "mean", "sum",
                "chi phí", "cost", "ngân sách", "budget", "expense", "expenditure",
                "đi muộn", "late", "muộn", "chậm", "tardiness",
                "tổng quan", "overview", "general", "summary",
                "tìm kiếm", "search", "tìm", "find", "lookup",
                "thông tin của", "info of", "detail of", "information about",
                "nhân viên tên", "employee named", "staff named"
        };

        boolean containsHRMKeywords = Arrays.stream(hrmKeywords)
                .anyMatch(lower::contains);

        boolean hasHRMPattern =
                lower.matches(".*\\b(của tôi|tôi có|tôi được|thông tin tôi)\\b.*") ||
                        lower.matches(".*\\b(team|nhóm|phòng ban|department)\\b.*") ||
                        lower.matches(".*\\b(báo cáo|thống kê|report|dashboard)\\b.*") ||
                        lower.matches(".*\\b(lương|salary|wage)\\b.*") ||
                        lower.matches(".*\\b(chấm công|attendance|timesheet)\\b.*") ||
                        lower.matches(".*\\b(nghỉ phép|leave|vacation)\\b.*") ||
                        lower.matches(".*\\b(hợp đồng|contract)\\b.*") ||
                        lower.matches(".*\\b(thưởng|bonus|khen thưởng)\\b.*") ||
                        lower.matches(".*\\b(vi phạm|violation|phạt)\\b.*") ||
                        lower.matches(".*\\b(danh sách|tất cả|toàn bộ|all)\\b.*") ||
                        lower.matches(".*\\b(top|cao nhất|thấp nhất|ranking)\\b.*") ||
                        lower.matches(".*\\b(phân tích|analysis|so sánh|compare)\\b.*") ||
                        lower.matches(".*\\b(tổng|total|trung bình|average)\\b.*") ||
                        lower.matches(".*\\b(thông tin của|nhân viên.*tên)\\b.*");

        log.info("🔍 === HRM DETECTION DEBUG ===");
        log.info("🔍 Original: '{}'", message);
        log.info("🔍 Keywords match: {}", containsHRMKeywords);
        log.info("🔍 Pattern match: {}", hasHRMPattern);
        log.info("🔍 Final result: {}", (containsHRMKeywords || hasHRMPattern));

        if (lower.contains("thưởng")) {
            log.info("🔍 ✅ 'thưởng' keyword found!");
        }
        if (lower.contains("vi phạm")) {
            log.info("🔍 ✅ 'vi phạm' keyword found!");
        }
        if (lower.contains("của tôi")) {
            log.info("🔍 ✅ 'của tôi' pattern found!");
        }
        if (lower.contains("danh sách")) {
            log.info("🔍 ✅ 'danh sách' keyword found!");
        }

        return containsHRMKeywords || hasHRMPattern;
    }

    private String processHRMQuestion(String question, NhanVien currentUser, UserRole userRole) {
        log.info("🔄 === PROCESSING HRM QUESTION ===");
        log.info("🔄 Question: '{}'", question);
        log.info("🔄 User: {} (ID: {})", currentUser.getHoTen(), currentUser.getId());
        log.info("🔄 Role: {}", userRole);

        try {
            // Check component availability
            if (hrmTranslator == null) {
                log.error("❌ hrmTranslator is NULL!");
                return "❌ HRM Translator không khả dụng.";
            }

            if (databaseExecutor == null) {
                log.error("❌ databaseExecutor is NULL!");
                return "❌ Database Executor không khả dụng.";
            }

            if (responseFormatter == null) {
                log.error("❌ responseFormatter is NULL!");
                return "❌ Response Formatter không khả dụng.";
            }

            // Step 1: Translate question to SQL
            log.info("🔄 Step 1: Translating to SQL...");
            QueryResult queryResult;

            try {
                queryResult = hrmTranslator.translateToSQL(question);
                log.info("🔄 Successfully called translateToSQL method");
            } catch (Exception e) {
                log.error("❌ Translation error: {}", e.getMessage(), e);
                return "❌ Lỗi dịch câu hỏi: " + e.getMessage();
            }

            if (queryResult == null) {
                log.error("❌ QueryResult is null after translation");
                return "❌ Không thể dịch câu hỏi.";
            }

            log.info("🔄 Translation result:");
            log.info("🔄   QueryType: {}", queryResult.getQueryType());
            log.info("🔄   SQL: {}", queryResult.getSqlQuery());

            // ✅ IMPROVED: Better success check
            boolean isTranslationSuccess = (queryResult.getQueryType() != QueryType.UNKNOWN &&
                    queryResult.getSqlQuery() != null &&
                    !queryResult.getSqlQuery().trim().isEmpty());

            if (queryResult.getQueryType() == QueryType.PERMISSION_DENIED) {
                log.warn("🔄 ⚠️ Permission denied for query");
                return "❌ Bạn không có quyền truy cập thông tin này.";
            }

            log.info("🔄   Translation Success: {}", isTranslationSuccess);

            if (!isTranslationSuccess) {
                log.warn("🔄 ⚠️ Translation failed - returning help message");
                return getHelpMessage(userRole);
            }

            // Step 2: Execute database query
            log.info("🔄 Step 2: Executing database query...");
            DatabaseResponse dbResponse = databaseExecutor.executeQuery(queryResult);

            log.info("🔄 Database execution:");
            log.info("🔄   Success: {}", dbResponse.isSuccess());
            log.info("🔄   Row count: {}", dbResponse.getData() != null ? dbResponse.getData().size() : 0);

            if (!dbResponse.isSuccess()) {
                log.error("🔄   Database Error: {}", dbResponse.getError());
                return "❌ Lỗi truy vấn cơ sở dữ liệu: " + dbResponse.getError();
            }

            if (dbResponse.getData() == null || dbResponse.getData().isEmpty()) {
                log.warn("🔄   No data found");
                return "📭 Không tìm thấy dữ liệu phù hợp với câu hỏi của bạn.";
            }

            // Step 3: Format response for user
            log.info("🔄 Step 3: Formatting response...");
            String formattedResponse = responseFormatter.formatResponse(dbResponse);

            log.info("🔄 Final response:");
            log.info("🔄   Response length: {}", formattedResponse.length());

            if (formattedResponse.length() > 0) {
                String preview = formattedResponse.substring(0, Math.min(formattedResponse.length(), 200));
                log.info("🔄   Preview: {}", preview);
            }

            return formattedResponse;

        } catch (Exception e) {
            log.error("❌ ProcessHRMQuestion error: {}", e.getMessage(), e);
            return "❌ Đã xảy ra lỗi khi xử lý câu hỏi HRM: " + e.getMessage();
        }
    }

    // ✅ NEW: Role-based help messages
    private String getHelpMessage(UserRole userRole) {
        return switch (userRole) {
            case ADMIN -> """
                ❓ Với quyền ADMIN, bạn có thể hỏi:
                
                🔍 **Tìm kiếm nhân viên:**
                • "Thông tin của nhân viên tên là Nguyễn Văn An"
                • "Nhân viên có mã 1005"
                
                📋 **Quản lý nhân viên:**
                • "Danh sách tất cả nhân viên trong công ty"
                • "Top 10 nhân viên có lương cao nhất tháng 4/2025"
                
                📊 **Báo cáo và thống kê:**
                • "Báo cáo lương theo phòng ban"
                • "Lương trung bình theo phòng ban"
                • "Nhân viên đi muộn nhiều nhất tháng 6"
                • "Thống kê tổng quan công ty"
                • "Phân tích chi phí nhân sự tháng 6"
                • "Tổng chi phí lương công ty"
                
                📋 **Thông tin cá nhân:**
                • "Lương của tôi"
                • "Thông tin cá nhân của tôi"
                • "Chấm công của tôi"
                """;
            case MANAGER -> """
                ❓ Với quyền MANAGER, bạn có thể hỏi:
                
                👥 **Quản lý team:**
                • "Danh sách nhân viên trong team"
                • "Báo cáo chấm công team"
                
                📋 **Thông tin cá nhân:**
                • "Lương của tôi"
                • "Thông tin cá nhân của tôi"
                • "Chấm công của tôi"
                """;
            default -> """
                ❓ Bạn có thể hỏi về:
                
                📋 **Thông tin cá nhân:**
                • "Lương tháng 4 của tôi"
                • "Số ngày phép còn lại của tôi"
                • "Thông tin chấm công tháng này"
                • "Hợp đồng lao động của tôi"
                • "Thưởng của tôi"
                • "Vi phạm của tôi"
                """;
        };
    }

    private String processGeneralQuestion(String question, String username) {
        try {
            log.info("💬 Processing as general chat question for user: {}", username);
            String contextualPrompt = buildHRMContextPrompt(question);
            return generalChatService.processMessage(contextualPrompt, username);
        } catch (Exception e) {
            log.error("❌ General chat error: {}", e.getMessage(), e);
            return "❌ Không thể xử lý câu hỏi: " + e.getMessage();
        }
    }

    private String buildHRMContextPrompt(String originalQuestion) {
        return """
            Bạn là trợ lý AI cho hệ thống quản lý nhân sự (HRM). 
            Nếu câu hỏi liên quan đến HR/nhân sự, hãy gợi ý người dùng hỏi cụ thể hơn về:
            - Thông tin cá nhân (lương, chấm công, nghỉ phép)
            - Quản lý nhân viên (nếu là manager)
            - Báo cáo HR (nếu là HR/Admin)
            
            Câu hỏi: """ + originalQuestion;
    }

    // Public utility method for testing
    public boolean testHRMDetection(String message) {
        return isHRMQuestion(message);
    }

    // Debug method
    public String getDebugInfo() {
        NhanVien currentUser = permissionService.getCurrentUser();
        UserRole userRole = permissionService.getCurrentUserRole();

        return String.format("""
            🔧 HRM Chat Service Debug Info:
            👤 Current User: %s (ID: %s)
            🎭 User Role: %s
            🔗 Components Status:
            - HRM Translator: %s
            - Database Executor: %s  
            - Response Formatter: %s
            - Permission Service: %s
            """,
                currentUser != null ? currentUser.getHoTen() : "NULL",
                currentUser != null ? currentUser.getId() : "NULL",
                userRole,
                hrmTranslator != null ? "✅ Available" : "❌ NULL",
                databaseExecutor != null ? "✅ Available" : "❌ NULL",
                responseFormatter != null ? "✅ Available" : "❌ NULL",
                permissionService != null ? "✅ Available" : "❌ NULL"
        );
    }
}
package com.example.hrm.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/ai-chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private HRMChatService hrmChatService; // ← THAY ĐỔI CHÍNH: Từ ChatService → HRMChatService

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody @Valid ChatRequest request,
            Authentication authentication) {

        System.out.println("🚀 === HRM AI ChatController.sendMessage() ===");
        System.out.println("📨 Request: " + request.getMessage());
        System.out.println("🕐 Time: " + getCurrentDateTime());

        try {
            // Safe authentication handling với fallback
            String username = "anonymous";
            if (authentication != null && authentication.getName() != null) {
                username = authentication.getName();
            }

            System.out.println("👤 User: " + username);

            // ← THAY ĐỔI: Gọi HRMChatService thay vì ChatService
            String response = hrmChatService.processMessage(request.getMessage(), username);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("status", "success");
            result.put("timestamp", System.currentTimeMillis());
            result.put("user", username);
            result.put("datetime", getCurrentDateTime());

            System.out.println("✅ Response sent successfully to user: " + username);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Validation error for user " + getUsername(authentication) + ": " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("response", e.getMessage());
            error.put("status", "validation_error");
            error.put("timestamp", System.currentTimeMillis());
            error.put("datetime", getCurrentDateTime());

            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            System.err.println("❌ HRM AI ChatController error: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("response", "❌ Lỗi hệ thống: Không thể xử lý yêu cầu của bạn lúc này");
            error.put("status", "error");
            error.put("timestamp", System.currentTimeMillis());
            error.put("datetime", getCurrentDateTime());
            error.put("user", getUsername(authentication));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ← THÊM MỚI: Test endpoint để debug HRM detection
    @PostMapping("/test-detection")
    public ResponseEntity<Map<String, Object>> testHRMDetection(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        System.out.println("🧪 Testing HRM detection...");

        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message không được để trống");
            }

            boolean isHRM = hrmChatService.testHRMDetection(message);

            Map<String, Object> result = new HashMap<>();
            result.put("message", message);
            result.put("isHRMQuestion", isHRM);
            result.put("detectedType", isHRM ? "HRM_QUESTION" : "GENERAL_QUESTION");
            result.put("timestamp", System.currentTimeMillis());
            result.put("datetime", getCurrentDateTime());
            result.put("user", getUsername(authentication));

            System.out.println("🔍 Detection result: " + (isHRM ? "HRM" : "GENERAL"));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("❌ Test detection error: " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            error.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(error);
        }
    }

    // Health check endpoint - cập nhật
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "hrm-ai-chat"); // ← Cập nhật tên service
        health.put("timestamp", System.currentTimeMillis());
        health.put("datetime", getCurrentDateTime());
        health.put("version", "2.0"); // ← Version với HRM support

        return ResponseEntity.ok(health);
    }

    // ← THÊM MỚI: Debug endpoint
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugInfo(Authentication authentication) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("currentUser", getUsername(authentication));
        debug.put("isAuthenticated", authentication != null);
        debug.put("timestamp", System.currentTimeMillis());
        debug.put("datetime", getCurrentDateTime());
        debug.put("service", "hrm-ai-chat");

        return ResponseEntity.ok(debug);
    }

    // Helper methods
    private String getUsername(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ChatRequest class giữ nguyên
    public static class ChatRequest {
        @NotBlank(message = "Tin nhắn không được để trống")
        @Size(max = 1000, message = "Tin nhắn không được quá 1000 ký tự")
        private String message;

        @Size(max = 50, message = "User ID không được quá 50 ký tự")
        private String userId;

        // Default constructor
        public ChatRequest() {}

        // Constructor
        public ChatRequest(String message, String userId) {
            this.message = message;
            this.userId = userId;
        }

        // Getters and Setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "ChatRequest{" +
                    "message='" + (message != null ?
                    (message.length() > 50 ? message.substring(0, 50) + "..." : message)
                    : "null") + '\'' +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }
}
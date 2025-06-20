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


import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/ai-chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody @Valid ChatRequest request,
            Authentication authentication) {

        System.out.println("🚀 === AI ChatController.sendMessage() ===");
        System.out.println("📨 Request: " + request.getMessage());

        try {
            // Safe authentication handling
            String username = "anonymous";
            if (authentication != null && authentication.getName() != null) {
                username = authentication.getName();
            }

            String response = chatService.processMessage(request.getMessage(), username);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("status", "success");
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Validation error: " + e.getMessage());

            Map<String, Object> error = new HashMap<>();
            error.put("response", e.getMessage());
            error.put("status", "validation_error");
            error.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            System.err.println("❌ AI ChatController error: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("response", "❌ Lỗi hệ thống: Không thể xử lý yêu cầu");
            error.put("status", "error");
            error.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "ai-chat");
        health.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(health);
    }

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
                    "message='" + (message != null ? message.substring(0, Math.min(message.length(), 50)) + "..." : "null") + '\'' +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }
}
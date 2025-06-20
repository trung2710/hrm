package com.example.hrm.ai;

import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_RESPONSE_LENGTH = 5000;

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private UserRepository userRepository;

    public String processMessage(String userMessage, String username) {
        log.info("🔥 === ChatService.processMessage() ===");
        log.info("📝 Input message length: {}", userMessage != null ? userMessage.length() : 0);
        log.info("👤 Username: {}", username);

        try {
            // Input validation
            validateInput(userMessage);

            String cleanMessage = userMessage.trim();

            // Optional: Get user context (if needed for personalization)
            String userContext = getUserContext(username);

            // Build prompt
            String prompt = buildPrompt(cleanMessage, userContext);
            log.info("📤 Prompt sent to Gemini (length: {})", prompt.length());

            // Call Gemini API
            String response = geminiAIService.generateContent(prompt);
            log.info("📥 Gemini response received (length: {})", response != null ? response.length() : 0);

            // Validate and process response
            String processedResponse = processResponse(response);

            log.info("✅ ChatService returning response");
            return processedResponse;

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Validation error for user {}: {}", username, e.getMessage());
            throw e; // Re-throw validation errors

        } catch (Exception e) {
            log.error("❌ ChatService Exception for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Không thể xử lý tin nhắn: " + e.getMessage());
        }
    }

    private void validateInput(String message) {
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Tin nhắn không được để trống");
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Tin nhắn quá dài (tối đa " + MAX_MESSAGE_LENGTH + " ký tự)");
        }

        // Check for potentially harmful content
        if (containsHarmfulContent(message)) {
            throw new IllegalArgumentException("Tin nhắn chứa nội dung không phù hợp");
        }
    }

    private boolean containsHarmfulContent(String message) {
        String lowerMessage = message.toLowerCase();
        // Basic harmful content detection - customize as needed
        String[] harmfulKeywords = {"hack", "virus", "malware", "exploit"};

        for (String keyword : harmfulKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String getUserContext(String username) {
        try {
            if (!"anonymous".equals(username)) {
                // Optional: Get user info for personalization
                // NhanVien user = userRepository.findByUsername(username);
                // return user != null ? "Người dùng: " + user.getHoTen() : "";
            }
        } catch (Exception e) {
            log.warn("Could not get user context for {}: {}", username, e.getMessage());
        }
        return "";
    }

    private String buildPrompt(String userMessage, String userContext) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là một trợ lý AI thông minh và hữu ích. ");
        prompt.append("Hãy trả lời bằng tiếng Việt một cách tự nhiên, chi tiết và chính xác. ");

        if (StringUtils.hasText(userContext)) {
            prompt.append(userContext).append(". ");
        }

        prompt.append("Câu hỏi: ").append(userMessage);

        return prompt.toString();
    }

    private String processResponse(String response) {
        if (!StringUtils.hasText(response)) {
            log.warn("⚠️ Gemini returned empty response!");
            return "Xin lỗi, tôi không thể trả lời câu hỏi này lúc này. Vui lòng thử lại!";
        }

        String cleanResponse = response.trim();

        // Limit response length
        if (cleanResponse.length() > MAX_RESPONSE_LENGTH) {
            cleanResponse = cleanResponse.substring(0, MAX_RESPONSE_LENGTH) + "...";
            log.info("Response truncated to {} characters", MAX_RESPONSE_LENGTH);
        }

        return cleanResponse;
    }

    // Optional: Method to handle different types of requests
    public String processSpecialMessage(String message, String username, String messageType) {
        log.info("Processing special message type: {}", messageType);

        switch (messageType.toLowerCase()) {
            case "greeting":
                return "Xin chào! Tôi là trợ lý AI, tôi có thể giúp gì cho bạn?";
            case "help":
                return "Tôi có thể giúp bạn trả lời câu hỏi, giải thích khái niệm, hoặc thảo luận về nhiều chủ đề khác nhau. Hãy đặt câu hỏi cho tôi!";
            default:
                return processMessage(message, username);
        }
    }
}
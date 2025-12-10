package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.ChatRequest;
import base.api.dto.response.ChatResponse;
import base.api.dto.response.ChatHistoryResponse;
import base.api.dto.response.TFUResponse;
import base.api.service.IChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController extends BaseAPIController {

    private final IChatbotService chatbotService;

    @PostMapping("/send-message")
    public ResponseEntity<TFUResponse<ChatResponse>> sendMessage(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatbotService.sendMessage(request);
            return success(response, "Message sent successfully");
        } catch (Exception e) {
            return badRequest("Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.");
        }
    }

    @GetMapping("/chat-history")
    public ResponseEntity<TFUResponse<List<ChatHistoryResponse>>> getChatHistory(
            @RequestParam(defaultValue = "1") Long userId) {
        try {
            List<ChatHistoryResponse> history = chatbotService.getChatHistory(1L);
            return success(history, "Chat history retrieved successfully");
        } catch (Exception e) {
            return badRequest("Không thể tải lịch sử chat. Vui lòng thử lại sau.");
        }
    }
}
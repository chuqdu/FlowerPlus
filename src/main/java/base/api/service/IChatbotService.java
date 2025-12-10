package base.api.service;

import base.api.dto.request.ChatRequest;
import base.api.dto.response.ChatResponse;
import base.api.dto.response.ChatHistoryResponse;
import java.util.List;

public interface IChatbotService {
    ChatResponse sendMessage(ChatRequest request);
    List<ChatHistoryResponse> getChatHistory(Long userId);
}
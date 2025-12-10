package base.api.service.impl;

import base.api.dto.request.ChatRequest;
import base.api.dto.response.ChatResponse;
import base.api.dto.response.ChatHistoryResponse;
import base.api.service.IChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ChatbotService implements IChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        try {
            StringBuilder urlBuilder = new StringBuilder("https://good-fun.org/?query=");
            urlBuilder.append(java.net.URLEncoder.encode(request.getMessage(), "UTF-8"));
            
            // Add user_id parameter (default to 1)
            urlBuilder.append("&user_id=").append(request.getUserId() != null ? request.getUserId() : 1);
            
            // Add image_url parameter if provided
            if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
                urlBuilder.append("&image_url=").append(java.net.URLEncoder.encode(request.getImageUrl(), "UTF-8"));
            }
            
            String url = urlBuilder.toString();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String message = jsonNode.get("message").asText();
                return new ChatResponse(message);
            } else {
                return new ChatResponse("Xin lỗi, tôi không thể trả lời câu hỏi này lúc này.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.");
        }
    }

    @Override
    public List<ChatHistoryResponse> getChatHistory(Long userId) {
        try {
            String url = "https://good-fun.org/chat_history?user_id=" + (userId != null ? userId : 1);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                List<ChatHistoryResponse> history = new ArrayList<>();
                
                // Check if response has chat_history array
                if (rootNode.has("chat_history") && rootNode.get("chat_history").isArray()) {
                    JsonNode chatHistoryArray = rootNode.get("chat_history");
                    
                    for (JsonNode item : chatHistoryArray) {
                        String userMessage = item.get("user_chat").asText();
                        String botResponse = item.get("response").asText();
                        String timestamp = item.get("created_at").asText();
                        
                        history.add(new ChatHistoryResponse(userMessage, botResponse, timestamp));
                    }
                }
                
                return history;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
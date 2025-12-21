package base.api.service.impl;

import base.api.dto.request.ChatRequest;
import base.api.dto.request.ChatbotApiRequest;
import base.api.dto.response.ChatResponse;
import base.api.dto.response.ChatHistoryResponse;
import base.api.service.IChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService implements IChatbotService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        try {
            String url = "https://good-fun.org/";
            
            // Generate session_id (you can implement your own logic)
            String sessionId = "session_" + System.currentTimeMillis();
            
            // Create request DTO
            ChatbotApiRequest apiRequest = new ChatbotApiRequest(
                request.getMessage(),
                request.getUserId() != null ? request.getUserId().toString() : "1",
                sessionId,
                request.getImageUrl()
            );
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity
            HttpEntity<ChatbotApiRequest> entity = new HttpEntity<>(apiRequest, headers);
            
            // Log request for debugging
            log.info("Sending chatbot request to: {}", url);
            log.info("Request body: {}", objectMapper.writeValueAsString(apiRequest));
            
            // Send POST request
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            log.info("Chatbot response status: {}", response.getStatusCode());
            log.info("Chatbot response body: {}", response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String message = jsonNode.get("message").asText();
                return new ChatResponse(message);
            } else {
                return new ChatResponse("Xin lỗi, tôi không thể trả lời câu hỏi này lúc này.");
            }
        } catch (Exception e) {
            log.error("Error calling chatbot API: {}", e.getMessage(), e);
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
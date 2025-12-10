package base.api.dto.response;

import lombok.Data;

@Data
public class ChatResponse {
    private String message;
    private String timestamp;
    
    public ChatResponse(String message) {
        this.message = message;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}
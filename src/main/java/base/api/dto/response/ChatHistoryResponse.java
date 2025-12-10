package base.api.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ChatHistoryResponse {
    private String userMessage;
    private String botResponse;
    private String timestamp;
    
    public ChatHistoryResponse(String userMessage, String botResponse, String timestamp) {
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.timestamp = timestamp;
    }
}
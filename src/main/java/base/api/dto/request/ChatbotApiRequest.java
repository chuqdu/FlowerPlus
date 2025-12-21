package base.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatbotApiRequest {
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("image_url")
    private String imageUrl;
    
    public ChatbotApiRequest(String query, String userId, String sessionId, String imageUrl) {
        this.query = query;
        this.userId = userId;
        this.sessionId = sessionId;
        this.imageUrl = imageUrl;
    }
}
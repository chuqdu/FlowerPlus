package base.api.dto.request;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private Long userId;
    private String imageUrl;
}
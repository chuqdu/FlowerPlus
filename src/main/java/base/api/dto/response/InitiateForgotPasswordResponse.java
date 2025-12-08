package base.api.dto.response;

import lombok.Data;

@Data
public class InitiateForgotPasswordResponse {
    private String resetToken;
    private String message;
}

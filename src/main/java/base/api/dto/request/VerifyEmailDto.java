package base.api.dto.request;

import lombok.Data;

@Data
public class VerifyEmailDto {
    private String verificationToken;
    private String verificationCode;
}

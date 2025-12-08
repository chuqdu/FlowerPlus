package base.api.dto.request;

import lombok.Data;

@Data
public class CompleteForgotPasswordDto {
    private String resetToken;
    private String verificationCode;
    private String newPassword;
    private String confirmNewPassword;
}

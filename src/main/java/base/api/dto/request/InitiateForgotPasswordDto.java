package base.api.dto.request;

import lombok.Data;

@Data
public class InitiateForgotPasswordDto {
    private String contactInfo; // email hoặc username
}

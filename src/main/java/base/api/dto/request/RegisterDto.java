package base.api.dto.request;

import lombok.Data;

@Data
public class RegisterDto {
    private String userName;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}

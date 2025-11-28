package base.api.dto.request;

import base.api.enums.UserRole;
import lombok.Data;

@Data
public class RegisterDto {
    private String userName;
    private String password;
    private String email;
    private String firstName;
    private String phone;
    private String lastName;
    private UserRole role;
}

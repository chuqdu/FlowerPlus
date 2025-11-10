package base.api.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterRequest {
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private LocalDateTime birthDate;

}

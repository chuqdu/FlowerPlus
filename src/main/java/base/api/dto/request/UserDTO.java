package base.api.dto.request;

import base.api.enums.UserGender;
import base.api.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    public String userName;

    public String phone;

    public String firstName;

    public UserGender gender;

    public String lastName;

    public LocalDateTime birthDate;

    public String avatar;

    public boolean isActive = true;

    public String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private UserRole role;
}

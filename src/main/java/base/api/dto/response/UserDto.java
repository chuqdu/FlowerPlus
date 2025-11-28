package base.api.dto.response;

import base.api.entity.DeliveryAddressModel;
import base.api.enums.UserGender;
import base.api.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<DeliveryAddressModel> deliveryAddresses = new ArrayList<>();

}

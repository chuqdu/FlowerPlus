package base.api.service;

import base.api.dto.request.DeliveryAddressDto;
import base.api.dto.request.RegisterDto;
import base.api.entity.UserModel;

public interface IUserService {
    UserModel createUser(UserModel model);
    UserModel findByUserName(String userName);
    boolean existedByEmail(String email);
    UserModel findById(Long id);
    UserModel registerUser(RegisterDto dto);
    UserModel createUpdateUserAddress(DeliveryAddressDto dto);
    boolean deleteDeliveryAddress(Long id, Long userId);
}

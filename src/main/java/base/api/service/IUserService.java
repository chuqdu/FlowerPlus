package base.api.service;

import base.api.dto.request.RegisterDto;
import base.api.entity.user.UserModel;

public interface IUserService {
    UserModel createUser(UserModel model);
    UserModel findByUserName(String userName);
    boolean existedByEmail(String email);
    UserModel findById(Long id);
    UserModel registerUser(RegisterDto dto);


}

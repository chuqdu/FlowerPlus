package base.api.service;

import base.api.dto.request.CompleteForgotPasswordDto;
import base.api.dto.request.DeliveryAddressDto;
import base.api.dto.request.RegisterDto;
import base.api.dto.request.UpdateProfileDto;
import base.api.dto.response.InitiateForgotPasswordResponse;
import base.api.entity.UserModel;

import java.util.List;

public interface IUserService {
    UserModel createUser(UserModel model);
    UserModel findByUserName(String userName);
    boolean existedByEmail(String email);
    UserModel findById(Long id);
    UserModel registerUser(RegisterDto dto);
    UserModel createUpdateUserAddress(DeliveryAddressDto dto);
    boolean deleteDeliveryAddress(Long id, Long userId);
    List<UserModel> getAllUsers();
    InitiateForgotPasswordResponse initiateForgotPassword(String contactInfo) throws Exception;
    void completeForgotPassword(CompleteForgotPasswordDto dto) throws Exception;
    void verifyEmail(base.api.dto.request.VerifyEmailDto dto) throws Exception;
    UserModel updateProfile(Long userId, UpdateProfileDto dto);
    void changePassword(Long userId, base.api.dto.request.ChangePasswordDto dto) throws Exception;
}

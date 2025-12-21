package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.config.JwtUtil;
import base.api.dto.request.*;
import base.api.dto.response.AuthResponse;
import base.api.dto.response.TFUResponse;
import base.api.dto.response.UserDto;
import base.api.entity.UserModel;
import base.api.service.IUserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseAPIController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ModelMapper mapper;


    @PostMapping("login")
    public ResponseEntity<TFUResponse<AuthResponse>> login(@RequestBody AuthRequest dto)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(),dto.getPassword()));

        UserModel user = userService.findByUserName(dto.getUsername());

        if(user == null){
            return badRequest("Không tìm thấy user");
        }

//        // Kiểm tra email đã xác thực chưa
//        if(!user.isVerified()){
//            return badRequest("Vui lòng xác thực email trước khi đăng nhập. Kiểm tra hộp thư của bạn.");
//        }

        String jwt = jwtUtil.generateToken(user);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(jwt);
        return success(authResponse);
    }

    @PostMapping("register")
    public ResponseEntity<TFUResponse<UserModel>> register(@RequestBody RegisterDto dto){
        UserModel user = userService.registerUser(dto);
        if(user == null ){
            return badRequest("Không tạo được user");
        }
        return success(user);
    }


    @GetMapping("get-user-by-id")
    public ResponseEntity<TFUResponse<UserModel>> getUserById(@RequestParam Long id){
        UserModel user = userService.findById(id);
        if(user == null){
            return badRequest("Không tìm thấy user");
        }
        return success(user);
    }
    @GetMapping("me")
    public ResponseEntity<TFUResponse<UserDto>> getUserInfo(){
        UserModel user = userService.findById(getCurrentUserId());
        if(user == null){
            return badRequest("Không tìm thấy user");
        }

        UserDto userDto = mapper.map(user, UserDto.class);

        return success(userDto);
    }

    @PostMapping("create-update-address")
    public ResponseEntity<TFUResponse<UserModel>> createUpdateAddress(@RequestBody DeliveryAddressDto dto){
        dto.setUserId(getCurrentUserId());
        UserModel user = userService.createUpdateUserAddress(dto);
        if(user == null){
            return badRequest("Không cập nhật được địa chỉ");
        }
        return success(user);
    }

    @GetMapping("get-list-users")
    public ResponseEntity<TFUResponse<Iterable<UserModel>>> getListUsers(){
        Iterable<UserModel> users = userService.getAllUsers();
        return success(users);
    }

    @PostMapping("forgot-password/initiate")
    public ResponseEntity<TFUResponse<base.api.dto.response.InitiateForgotPasswordResponse>> initiateForgotPassword(
            @RequestBody base.api.dto.request.InitiateForgotPasswordDto dto) {
        try {
            base.api.dto.response.InitiateForgotPasswordResponse response = userService.initiateForgotPassword(dto.getContactInfo());
            return success(response);
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping("forgot-password/complete")
    public ResponseEntity<TFUResponse<String>> completeForgotPassword(
            @RequestBody base.api.dto.request.CompleteForgotPasswordDto dto) {
        try {
            userService.completeForgotPassword(dto);
            return success("Đặt lại mật khẩu thành công");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping("verify-email")
    public ResponseEntity<TFUResponse<String>> verifyEmail(
            @RequestBody base.api.dto.request.VerifyEmailDto dto) {
        try {
            userService.verifyEmail(dto);
            return success("Xác thực email thành công");
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping("update-profile")
    public ResponseEntity<TFUResponse<UserModel>> updateProfile(@RequestBody UpdateProfileDto dto) {
        try {
            UserModel updatedUser = userService.updateProfile(getCurrentUserId(), dto);
            return success(updatedUser);
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }
}

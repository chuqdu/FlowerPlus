package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.config.JwtUtil;
import base.api.dto.request.*;
import base.api.dto.response.AuthResponse;
import base.api.dto.response.TFUResponse;
import base.api.dto.response.UserDto;
import base.api.entity.user.UserModel;
import base.api.service.IUserService;
import org.apache.catalina.mapper.Mapper;
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

}

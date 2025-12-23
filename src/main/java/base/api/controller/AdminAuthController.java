package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.config.JwtUtil;
import base.api.dto.request.AuthRequest;
import base.api.dto.response.AuthResponse;
import base.api.dto.response.TFUResponse;
import base.api.entity.UserModel;
import base.api.enums.UserRole;
import base.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-auth")
public class AdminAuthController extends BaseAPIController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("login")
    public ResponseEntity<TFUResponse<AuthResponse>> adminLogin(@RequestBody AuthRequest dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

            UserModel user = userService.findByUserName(dto.getUsername());

            if (user == null) {
                return badRequest("Không tìm thấy user");
            }

            if (!UserRole.ADMIN.equals(user.getRole()) && !UserRole.STAFF.equals(user.getRole()) && !UserRole.SHOP_OWNER.equals(user.getRole())) {
                return badRequest("Bạn không có quyền truy cập vào trang quản trị");
            }

            String jwt = jwtUtil.generateToken(user);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAccessToken(jwt);
            return success(authResponse, "Đăng nhập thành công");
        } catch (Exception e) {
            return badRequest("Tên đăng nhập hoặc mật khẩu không đúng");
        }
    }
}
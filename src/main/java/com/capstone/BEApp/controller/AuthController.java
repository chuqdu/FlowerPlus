package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.account.AccountDto;
import com.capstone.BEApp.dto.auth.LoginRequest;
import com.capstone.BEApp.dto.auth.LoginResponseDto;
import com.capstone.BEApp.dto.auth.RegisterRequest;
import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "Login và lấy JWT token")
    public ResponseDto<LoginResponseDto> login(@RequestBody LoginRequest request) {
        try {
            return authService.login(request);
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi đăng nhập: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @PermitAll
    @Operation(summary = "Tạo tài khoản user")
    public ResponseDto<String> register(@RequestBody RegisterRequest request) {
        try {
            return authService.register(request);
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi đăng ký: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    public ResponseDto<AccountDto> me(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean includeRole) {
        try {
            return authService.getMe(authentication, includeRole);
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi lấy thông tin người dùng: " + e.getMessage());
        }
    }
}

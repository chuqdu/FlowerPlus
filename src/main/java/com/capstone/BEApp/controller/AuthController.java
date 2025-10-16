package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.account.AccountDto;
import com.capstone.BEApp.dto.auth.LoginRequest;
import com.capstone.BEApp.dto.auth.LoginResponseDto;
import com.capstone.BEApp.dto.auth.RegisterRequest;
import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseDto<LoginResponseDto> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @Operation(summary = "Tạo tài khoản user")
    public ResponseDto<String> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public ResponseDto<AccountDto> me(Authentication authentication,
                                      @RequestParam(defaultValue = "false") boolean includeRole) {
        return authService.getMe(authentication, includeRole);
    }
}

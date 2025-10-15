package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.account.AccountDto;
import com.capstone.BEApp.dto.auth.LoginRequest;
import com.capstone.BEApp.dto.auth.LoginResponseDto;
import com.capstone.BEApp.dto.auth.RegisterRequest;
import com.capstone.BEApp.dto.common.ResponseDto;
import org.springframework.security.core.Authentication;

public interface AuthService {
    ResponseDto<LoginResponseDto> login(LoginRequest request);
    ResponseDto<String> register(RegisterRequest request);
    ResponseDto<AccountDto> getMe(Authentication authentication, boolean includeRole);
}

package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.account.AccountDto;
import com.capstone.BEApp.dto.auth.LoginRequest;
import com.capstone.BEApp.dto.auth.LoginResponseDto;
import com.capstone.BEApp.dto.auth.RegisterRequest;
import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.entity.Account;
import com.capstone.BEApp.entity.Role;
import com.capstone.BEApp.repository.AccountRepository;
import com.capstone.BEApp.repository.RoleRepository;
import com.capstone.BEApp.security.JwtTokenUtil;
import com.capstone.BEApp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenUtil jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public ResponseDto<LoginResponseDto> login(LoginRequest request) {
        var acc = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), acc.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(acc.getEmail(), acc.getRole().getName());

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setEmail(acc.getEmail());
        response.setRole(acc.getRole().getName());

        return ResponseDto.success(response, "Đăng nhập thành công");
    }

    @Override
    public ResponseDto<String> register(RegisterRequest request) {
        if (accountRepository.existsAccountByEmail(request.getEmail())) {
            return ResponseDto.fail("Email đã tồn tại");
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Role User"));

        Account acc = new Account();
        acc.setName(request.getName());
        acc.setEmail(request.getEmail());
        acc.setPassword(passwordEncoder.encode(request.getPassword()));
        acc.setPhone(request.getPhone());
        acc.setAge(request.getAge());
        acc.setGender(request.getGender());
        acc.setAddress(request.getAddress());
        acc.setRole(role);

        accountRepository.save(acc);

        return ResponseDto.success(null,"Đăng kí thành công");
    }

    public ResponseDto<AccountDto> getMe(Authentication authentication, boolean includeRole) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Account acc)) {
            return ResponseDto.fail("Unauthorized");
        }

        Optional<Account> accountOpt;
        if (includeRole) {
            accountOpt = accountRepository.findWithRoleByEmail(acc.getEmail());
        } else {
            accountOpt = accountRepository.findByEmail(acc.getEmail());
        }

        Account account = accountOpt.orElseThrow(() -> new RuntimeException("Account not found"));

        AccountDto dto = AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .age(account.getAge())
                .gender(account.getGender())
                .address(account.getAddress())
                .role(includeRole && account.getRole() != null ? account.getRole().getName() : null)
                .build();

        return ResponseDto.success(dto, "OK");
    }
}

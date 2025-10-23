package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.account.AccountDto;
import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseDto<AccountDto> getById(@PathVariable Long id) {
        try {
            AccountDto account = accountService.getById(id);
            return ResponseDto.success(account, "Lấy thông tin tài khoản thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi lấy tài khoản: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseDto<?> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<AccountDto> result = accountService.search(keyword, page - 1, size);
            return ResponseDto.successWithPagination(result.getContent(), "Tìm kiếm tài khoản thành công", result);
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi tìm kiếm tài khoản: " + e.getMessage());
        }
    }
}

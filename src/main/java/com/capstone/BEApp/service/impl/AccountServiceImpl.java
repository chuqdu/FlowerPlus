package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.account.AccountDto;
import com.capstone.BEApp.entity.Account;
import com.capstone.BEApp.repository.AccountRepository;
import com.capstone.BEApp.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public AccountDto getById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        return toDto(account);
    }

    @Override
    public Page<AccountDto> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Account> accounts = accountRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                        keyword, keyword, keyword, pageable);
        return accounts.map(this::toDto);
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .avatar(account.getAvatar())
                .age(account.getAge())
                .gender(account.getGender())
                .address(account.getAddress())
                .createdDate(account.getCreatedDate())
                .role(account.getRole() != null ? account.getRole().getName() : null)
                .build();
    }

}

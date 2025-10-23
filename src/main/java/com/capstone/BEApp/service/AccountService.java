package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.account.AccountDto;
import org.springframework.data.domain.Page;

public interface AccountService {
    AccountDto getById(Long id);
    Page<AccountDto> search(String keyword, int page, int size);
}

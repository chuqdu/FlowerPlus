package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAccountId(Long accountId);
}

package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Cart;
import com.capstone.BEApp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByAccount(Account account);
    void deleteByAccount(Account account);
}


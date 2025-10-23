package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    boolean existsAccountByEmail(String email);
    @EntityGraph(attributePaths = {"role"})
    Optional<Account> findWithRoleByEmail(String email);
    @EntityGraph(attributePaths = {"role"})
    Page<Account> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            String name, String email, String phone, Pageable pageable
    );
}

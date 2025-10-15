package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Account;
import com.capstone.BEApp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}

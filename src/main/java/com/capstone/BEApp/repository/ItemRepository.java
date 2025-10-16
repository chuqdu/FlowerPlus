package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Items;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Items, Long> {
    Page<Items> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}

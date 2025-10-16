package com.capstone.BEApp.repository;

import com.capstone.BEApp.entity.Flower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowerRepository extends JpaRepository<Flower, Long> {
    Page<Flower> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}

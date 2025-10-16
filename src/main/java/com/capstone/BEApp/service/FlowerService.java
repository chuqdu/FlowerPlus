package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.flower.FlowerDto;
import org.springframework.data.domain.Page;

public interface FlowerService {
    FlowerDto create(FlowerDto dto);
    FlowerDto update(Long id, FlowerDto dto);
    void delete(Long id);
    FlowerDto getById(Long id);
    Page<FlowerDto> search(String keyword, int page, int size);
}

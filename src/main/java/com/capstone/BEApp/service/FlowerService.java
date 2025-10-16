package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.flower.CreateFlowerDto;
import com.capstone.BEApp.dto.flower.FlowerDto;
import com.capstone.BEApp.dto.flower.UpdateFlowerDto;
import org.springframework.data.domain.Page;

public interface FlowerService {
    FlowerDto create(CreateFlowerDto dto);
    FlowerDto update(UpdateFlowerDto dto);
    void delete(Long id);
    FlowerDto getById(Long id);
    Page<FlowerDto> search(String keyword, int page, int size);
}

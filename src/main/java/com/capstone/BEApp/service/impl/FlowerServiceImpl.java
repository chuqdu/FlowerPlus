package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.flower.FlowerDto;
import com.capstone.BEApp.entity.Category;
import com.capstone.BEApp.entity.Flower;
import com.capstone.BEApp.repository.CategoryRepository;
import com.capstone.BEApp.repository.FlowerRepository;
import com.capstone.BEApp.service.FlowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FlowerServiceImpl implements FlowerService {

    private final FlowerRepository flowerRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public FlowerDto create(FlowerDto dto) {
        Flower flower = new Flower();
        flower.setName(dto.getName());
        flower.setDescription(dto.getDescription());
        flower.setPrice(dto.getPrice());
        flower.setQuality(dto.getQuality());
        flower.setStatus(dto.getStatus());
        flower.setSeason(dto.getSeason());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy danh mục"));
            flower.setCategory(category);
        }

        Flower saved = flowerRepository.save(flower);
        return toDto(saved);
    }

    @Override
    public FlowerDto update(Long id, FlowerDto dto) {
        Flower flower = flowerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hoa"));

        flower.setName(dto.getName());
        flower.setDescription(dto.getDescription());
        flower.setPrice(dto.getPrice());
        flower.setQuality(dto.getQuality());
        flower.setStatus(dto.getStatus());
        flower.setSeason(dto.getSeason());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy danh mục"));
            flower.setCategory(category);
        } else {
            flower.setCategory(null);
        }

        Flower saved = flowerRepository.save(flower);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!flowerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hoa để xóa");
        }
        flowerRepository.deleteById(id);
    }

    @Override
    public FlowerDto getById(Long id) {
        Flower flower = flowerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hoa"));
        return toDto(flower);
    }

    @Override
    public Page<FlowerDto> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Flower> result = flowerRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return result.map(this::toDto);
    }

    private FlowerDto toDto(Flower flower) {
        return FlowerDto.builder()
                .id(flower.getId())
                .name(flower.getName())
                .description(flower.getDescription())
                .price(flower.getPrice())
                .quality(flower.getQuality())
                .status(flower.getStatus())
                .season(flower.getSeason())
                .createdDate(flower.getCreatedDate())
                .categoryId(flower.getCategory() != null ? flower.getCategory().getId() : null)
                .categoryName(flower.getCategory() != null ? flower.getCategory().getName() : null)
                .build();
    }
}

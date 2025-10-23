package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.flower.CreateFlowerDto;
import com.capstone.BEApp.dto.flower.FlowerDto;
import com.capstone.BEApp.dto.flower.UpdateFlowerDto;
import com.capstone.BEApp.entity.Flower;
import com.capstone.BEApp.entity.Image;
import com.capstone.BEApp.repository.CategoryRepository;
import com.capstone.BEApp.repository.FlowerRepository;
import com.capstone.BEApp.service.FlowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowerServiceImpl implements FlowerService {

    private final FlowerRepository flowerRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public FlowerDto create(CreateFlowerDto dto) {
        Flower flower = new Flower();
        flower.setName(dto.getName());
        flower.setDescription(dto.getDescription());
        flower.setPrice(dto.getPrice());
        flower.setQuality(dto.getQuality());
        flower.setStatus("ACTIVE");
        flower.setSeason(dto.getSeason());

        if (dto.getImageUrls() != null) {
            List<Image> images = dto.getImageUrls().stream()
                    .map(url -> Image.builder().url(url).flower(flower).build())
                    .collect(Collectors.toList());
            flower.setImages(images);
        }

        Flower saved = flowerRepository.save(flower);
        return toDto(saved);
    }


    @Override
    public FlowerDto update(UpdateFlowerDto dto) {
        Flower flower = flowerRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hoa"));

        flower.setName(dto.getName());
        flower.setDescription(dto.getDescription());
        flower.setPrice(dto.getPrice());
        flower.setQuality(dto.getQuality());
        flower.setStatus(dto.getStatus());
        flower.setSeason(dto.getSeason());

        if (dto.getImageUrls() != null) {
            flower.getImages().clear();
            List<Image> newImages = dto.getImageUrls().stream()
                    .map(url -> Image.builder().url(url).flower(flower).build())
                    .collect(Collectors.toList());
            flower.setImages(newImages);
        }

        Flower saved = flowerRepository.save(flower);
        return toDto(saved);
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
        List<String> imageUrls = flower.getImages() != null
                ? flower.getImages().stream()
                .map(Image::getUrl)
                .collect(Collectors.toList())
                : null;

        return FlowerDto.builder()
                .id(flower.getId())
                .name(flower.getName())
                .description(flower.getDescription())
                .price(flower.getPrice())
                .quality(flower.getQuality())
                .status(flower.getStatus())
                .season(flower.getSeason())
                .createdDate(flower.getCreatedDate())
                .imageUrls(imageUrls)
                .build();
    }
}

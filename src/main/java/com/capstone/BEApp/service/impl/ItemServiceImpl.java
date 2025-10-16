package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.item.CreateItemDto;
import com.capstone.BEApp.dto.item.ItemDto;
import com.capstone.BEApp.dto.item.UpdateItemDto;
import com.capstone.BEApp.entity.Image;
import com.capstone.BEApp.entity.Items;
import com.capstone.BEApp.repository.ItemRepository;
import com.capstone.BEApp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemDto create(CreateItemDto dto) {
        Items item = new Items();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());

        if (dto.getImageIds() != null && !dto.getImageIds().isEmpty()) {
            List<Image> images = dto.getImageIds().stream()
                    .map(url -> Image.builder()
                            .url(url)
                            .items(item)
                            .build())
                    .toList();
            item.setImages(images);
        }

        Items saved = itemRepository.save(item);

        return toDto(saved);
    }



    @Override
    public ItemDto update(UpdateItemDto dto) {
        Items item = itemRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + dto.getId()));

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());

        if (item.getImages() != null && !item.getImages().isEmpty()) {
            item.getImages().clear();
        }

        if (dto.getImageIds() != null && !dto.getImageIds().isEmpty()) {
            List<Image> newImages = dto.getImageIds().stream()
                    .map(url -> Image.builder()
                            .url(url)
                            .items(item)
                            .build())
                    .toList();
            item.setImages(newImages);
        }

        Items updated = itemRepository.save(item);

        return toDto(updated);
    }


    @Override
    public ItemDto getById(Long id) {
        Items item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
        return toDto(item);
    }

    @Override
    public Page<ItemDto> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Items> result = itemRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return result.map(this::toDto);
    }

    private ItemDto toDto(Items item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrls(item.getImages() != null
                        ? item.getImages().stream()
                        .map(Image::getUrl)
                        .toList()
                        : List.of())
                .build();
    }
}

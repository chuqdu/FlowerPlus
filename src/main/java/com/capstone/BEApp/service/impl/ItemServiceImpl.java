package com.capstone.BEApp.service.impl;

import com.capstone.BEApp.dto.item.ItemDto;
import com.capstone.BEApp.entity.Items;
import com.capstone.BEApp.repository.ItemRepository;
import com.capstone.BEApp.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemDto create(ItemDto dto) {
        Items item = new Items();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());

        Items saved = itemRepository.save(item);
        return toDto(saved);
    }

    @Override
    public ItemDto update(Long id, ItemDto dto) {
        Items item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());

        Items saved = itemRepository.save(item);
        return toDto(saved);
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
                .build();
    }
}

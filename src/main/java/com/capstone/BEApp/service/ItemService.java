package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.item.ItemDto;
import org.springframework.data.domain.Page;

public interface ItemService {
    ItemDto create(ItemDto dto);
    ItemDto update(Long id, ItemDto dto);
    ItemDto getById(Long id);
    Page<ItemDto> search(String keyword, int page, int size);
}

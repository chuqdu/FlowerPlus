package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.item.CreateItemDto;
import com.capstone.BEApp.dto.item.ItemDto;
import com.capstone.BEApp.dto.item.UpdateItemDto;
import org.springframework.data.domain.Page;

public interface ItemService {
    ItemDto create(CreateItemDto dto);
    ItemDto update(UpdateItemDto dto);
    ItemDto getById(Long id);
    Page<ItemDto> search(String keyword, int page, int size);
}

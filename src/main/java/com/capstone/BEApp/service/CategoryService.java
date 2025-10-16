package com.capstone.BEApp.service;

import com.capstone.BEApp.dto.category.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDto create(CategoryDto dto);
    CategoryDto update(CategoryDto dto);
    CategoryDto getById(Long id);
    Page<CategoryDto> search(String keyword, int page, int size);
}

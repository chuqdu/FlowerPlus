package base.api.service;

import base.api.dto.request.CategoryNodeDto;
import base.api.dto.request.CreateCategoryDto;
import base.api.entity.CategoryModel;

import java.util.List;

public interface ICategoryService {
    CategoryModel getById(Long id);
    CategoryModel create(CreateCategoryDto dto);
    List<CategoryNodeDto> getCategoryTree();
}

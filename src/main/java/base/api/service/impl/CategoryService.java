package base.api.service.impl;

import base.api.dto.request.CategoryNodeDto;
import base.api.dto.request.CreateCategoryDto;
import base.api.entity.CategoryModel;
import base.api.entity.UserModel;
import base.api.enums.SyncStatus;
import base.api.repository.ICategoryRepository;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import base.api.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService  {
    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public CategoryModel getById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
    @Override
    @Transactional(readOnly = true)
    public List<CategoryNodeDto> getCategoryTree(Boolean isPublic) {
        List<CategoryModel> all = categoryRepository.findAll();

        if (isPublic != null) {
            all = all.stream()
                    .filter(c -> c.isPublic() == isPublic)
                    .collect(Collectors.toList());
        }

        // Map id -> node DTO
        Map<Long, CategoryNodeDto> map = new HashMap<>(all.size());
        for (CategoryModel c : all) {
            CategoryNodeDto node = new CategoryNodeDto();
            node.setId(c.getId());
            node.setName(c.getName());
            node.setDescription(c.getDescription());
            node.setPublic(c.isPublic());
            node.setParentId(c.getParent() == null ? null : c.getParent().getId());
            map.put(c.getId(), node);
        }

        // Gắn children
        List<CategoryNodeDto> roots = new ArrayList<>();
        for (CategoryModel c : all) {
            CategoryNodeDto node = map.get(c.getId());
            CategoryModel parent = c.getParent();
            if (parent == null || parent.getId().equals(c.getId()) || !map.containsKey(parent.getId())) {
                // nếu cha null / tự trỏ / không có trong tập
                roots.add(node);
            } else {
                map.get(parent.getId()).getChildren().add(node);
            }
        }

        // (Tuỳ chọn) sắp xếp children theo tên
        sortTreeByName(roots);

        return roots;
    }

    @Override
    public CategoryModel update(CreateCategoryDto dto) {
        CategoryModel model = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + dto.getId()));

        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setPublic(dto.isPublic);
        
        // Reset sync status when updating
        model.setSyncStatus(SyncStatus.PENDING);

        if (dto.getParentId() != null) {
            CategoryModel parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found: " + dto.getParentId()));
            model.setParent(parent);
        } else {
            model.setParent(null);
        }

        return categoryRepository.save(model);

    }

    private void sortTreeByName(List<CategoryNodeDto> nodes) {
        nodes.sort(Comparator.comparing(CategoryNodeDto::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        for (CategoryNodeDto n : nodes) {
            sortTreeByName(n.getChildren());
        }
    }

    @Override
    public CategoryModel create(CreateCategoryDto dto) {
        UserModel user = userRepository.findById(dto.getUserId()).orElse(null);
        if(user == null){
            return null;
        }

        CategoryModel model = new CategoryModel();
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setUserModel(user);
        model.setSyncStatus(SyncStatus.PENDING); // Set default sync status
        
        if (dto.getParentId() != null) {
            CategoryModel parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found: " + dto.getParentId()));
            model.setParent(parent);
            parent.getChildren().add(model);
        }

        return categoryRepository.save(model);
    }


}

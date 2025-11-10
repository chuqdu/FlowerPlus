package base.api.service.impl;

import base.api.dto.request.CategoryNodeDto;
import base.api.dto.request.CreateCategoryDto;
import base.api.entity.CategoryModel;
import base.api.entity.user.UserModel;
import base.api.repository.ICategoryRepository;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import base.api.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    public List<CategoryNodeDto> getCategoryTree() {
        List<CategoryModel> all = categoryRepository.findAll();

        // Map id -> node DTO
        Map<Long, CategoryNodeDto> map = new HashMap<>(all.size());
        for (CategoryModel c : all) {
            CategoryNodeDto node = new CategoryNodeDto();
            node.setId(c.getId());
            node.setName(c.getName());
            node.setDescription(c.getDescription());
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
        if (dto.getParentId() != null) {
            CategoryModel parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found: " + dto.getParentId()));
            model.setParent(parent);
            parent.getChildren().add(model);
        }

        return categoryRepository.save(model);
    }


}

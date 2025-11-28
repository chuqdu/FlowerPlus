package base.api.service;


import base.api.dto.request.ProductDto;
import base.api.dto.request.paging.PageableRequestDTO;
import base.api.dto.response.ProductResponse;
import base.api.entity.ProductModel;
import base.api.enums.ProductType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IProductService {
    ProductModel createProduct(ProductDto dto);
    ProductModel updateProduct(ProductDto dto);
    List<ProductModel> getListProduct();
    ProductResponse getProductById(Long id);
    String deleteProduct(Long id);
    Page<ProductResponse> getProducts(ProductType type, Boolean active, Long categoryId, Boolean custom, Long userId, PageableRequestDTO pageableRequest);
}

package base.api.repository;

import base.api.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IProductRepository  extends JpaRepository<ProductModel, Long>, JpaSpecificationExecutor<ProductModel>{}
package base.api.repository;

import base.api.entity.CartItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItemModel, Long> {
    List<CartItemModel> findByCart_Id(Long cartId);
    Optional<CartItemModel> findByCart_IdAndProductId(Long cartId, Long productId);
}

package base.api.service.impl;

import base.api.dto.request.CartDto;
import base.api.dto.request.CartItemRequest;
import base.api.dto.request.CartItemUpdateRequest;
import base.api.dto.request.CartResponse;
import base.api.entity.CartItemModel;
import base.api.entity.CartModel;
import base.api.entity.ProductModel;
import base.api.entity.user.UserModel;
import base.api.repository.ICartItemRepository;
import base.api.repository.ICartRepository;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import base.api.service.ICartService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CartService implements ICartService {

    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IUserRepository userRepository; // giả định đã có

    @Override
    public CartResponse getCartByUser(Long userId) {
        CartModel cart = getOrCreateCart(userId);

        return CartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        CartModel cart = getOrCreateCart(userId);


        CartItemModel item = cartItemRepository
                .findByCart_IdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);

        ProductModel product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (item == null) {
            item = new CartItemModel();
            item.setCart(cart);
            item.setProductId(request.getProductId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImages());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        } else {
            int addQty = request.getQuantity() != null ? request.getQuantity() : 1;
            item.setQuantity(item.getQuantity() + addQty);
        }

        item.recalcLineTotal();

        if (!cart.getCartItems().contains(item)) {
            cart.getCartItems().add(item);
        }

        recalcCartTotal(cart);
        cartRepository.save(cart);

        return CartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long cartItemId, CartItemUpdateRequest request) {
        CartModel cart = getOrCreateCart(userId);
        CartItemModel item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }

        item.recalcLineTotal();
        recalcCartTotal(cart);
        cartRepository.save(cart);

        return CartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        CartModel cart = getOrCreateCart(userId);
        CartItemModel item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        cart.getCartItems().remove(item);
        recalcCartTotal(cart);
        cartRepository.save(cart);

        return CartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long userId) {
        CartModel cart = getOrCreateCart(userId);
        cart.getCartItems().clear();
        cart.setTotalPrice(0.0);
        return CartMapper.toCartResponse(cart);
    }

    // Helper methods
    private CartModel getOrCreateCart(Long userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return cartRepository.findByUser_Id(userId).orElseGet(() -> {
            CartModel c = new CartModel();
            c.setUser(user);
            c.setTotalPrice(0.0);
            return cartRepository.save(c);
        });
    }

    private void recalcCartTotal(CartModel cart) {
        double total = cart.getCartItems().stream()
                .mapToDouble(CartItemModel::getLineTotal)
                .sum();
        cart.setTotalPrice(total);
    }
}

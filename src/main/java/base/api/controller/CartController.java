package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.CartDto;
import base.api.dto.request.CartItemRequest;
import base.api.dto.request.CartItemUpdateRequest;
import base.api.dto.request.CartResponse;
import base.api.dto.response.TFUResponse;
import base.api.entity.CartItemModel;
import base.api.entity.CartModel;
import base.api.service.ICartItemService;
import base.api.service.ICartService;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController extends BaseAPIController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private ICartItemService cartItemService;

    @GetMapping
    @PermitAll
    public ResponseEntity<TFUResponse<CartResponse>> getCart() {
        try {
            CartResponse data = cartService.getCartByUser(getCurrentUserId());
            if(data == null){
                return success(null);
            }
            return success(data);
        } catch (EntityNotFoundException e) {
            return badRequest("Không tìm thấy user");
        } catch (Exception e) {
            return badRequest("Lỗi khi lấy giỏ hàng: " + e.getMessage());
        }
    }

    // POST /api/carts/items?userId=123
    @PostMapping("/items")
    public ResponseEntity<TFUResponse<CartResponse>> addItem(@RequestBody @Valid CartItemRequest request) {
        try {
            CartResponse data = cartService.addItem(getCurrentUserId(), request);
            return success(data);
        } catch (EntityNotFoundException e) {
            return badRequest("Không tìm thấy user hoặc sản phẩm");
        } catch (MethodArgumentNotValidException | BindException e) {
            return badRequest("Dữ liệu không hợp lệ");
        } catch (Exception e) {
            return badRequest("Lỗi khi thêm sản phẩm vào giỏ: " + e.getMessage());
        }
    }

    // PATCH /api/carts/items/{itemId}
    @PutMapping("/items/{itemId}")
    public ResponseEntity<TFUResponse<CartResponse>> updateItem(
                                                                @PathVariable Long itemId,
                                                                @RequestBody @Valid CartItemUpdateRequest request) {
        try {
            CartResponse data = cartService.updateItem(getCurrentUserId(), itemId, request);
            return success(data);
        } catch (EntityNotFoundException e) {
            return badRequest("Không tìm thấy cart item");
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return badRequest("Lỗi khi cập nhật số lượng: " + e.getMessage());
        }
    }

    // DELETE /api/carts/items/{itemId}
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<TFUResponse<CartResponse>> removeItem(
                                                                @PathVariable Long itemId) {
        try {
            CartResponse data = cartService.removeItem(getCurrentUserId(), itemId);
            return success(data);
        } catch (EntityNotFoundException e) {
            return badRequest("Không tìm thấy cart item");
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return badRequest("Lỗi khi xoá sản phẩm khỏi giỏ: " + e.getMessage());
        }
    }

    // DELETE /api/carts/items
    @DeleteMapping("/items")
    public ResponseEntity<TFUResponse<CartResponse>> clear() {
        try {
            CartResponse data = cartService.clearCart(getCurrentUserId());
            return success(data);
        } catch (EntityNotFoundException e) {
            return badRequest("Không tìm thấy user");
        } catch (Exception e) {
            return badRequest("Lỗi khi xoá toàn bộ giỏ hàng: " + e.getMessage());
        }
    }
}

package base.api.service.impl;

import base.api.dto.request.CheckoutDto;
import base.api.dto.request.OrderDto;
import base.api.entity.*;
import base.api.repository.*;
import base.api.service.ICartService;
import base.api.service.IOrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;

import java.util.List;


@Service
public class OrderService implements IOrderService {

    @Autowired
    private PayOS payOS;

    @Autowired
    private ICartRepository cartRepo;

    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private IOrderRepository orderRepo;

    @Autowired
    private IOrderItemRepository orderItemRepository;

    @Autowired
    private ICartService cartService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private  ITransactionRepository txRepo;

    @Transactional
    @Override
    public String checkout(CheckoutDto dto) throws Exception {
        CartModel cart = cartRepo.findByUser_Id(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        if (cart.getCartItems().isEmpty()) throw new IllegalStateException("Cart is empty");

        // Tạo order
        OrderModel order = new OrderModel();
        order.setUser(cart.getUser());
        order.setOrderCode(String.valueOf(System.currentTimeMillis() / 1000));
        order.setStatus("UNPAID");
        order.setShippingAddress(dto.getShippingAddress());
        order.setPhoneNumber(dto.getPhoneNumber());
        order.setNote(dto.getNote());

        for (CartItemModel ci : cart.getCartItems()) {
            order.addItem(OrderItemModel.of(
                    ci.getProductId(),
                    ci.getProductName(),
                    ci.getProductImage(),
                    ci.getUnitPrice(),
                    ci.getQuantity()
            ));
        }
        order.recalcTotal();
        orderRepo.save(order);

        // Gọi PayOS
        int amount = (int) order.getTotal();
        long orderCode = System.currentTimeMillis() / 1000;

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description("TXN" + order.getOrderCode())
                .returnUrl(dto.getReturnUrl())
                .cancelUrl(dto.getCancelUrl())
                .build();

        CheckoutResponseData result = payOS.createPaymentLink(paymentData);

        // Lưu transaction
        TransactionModel tx = new TransactionModel();
        tx.setOrder(order);
        tx.setOrderCode(order.getOrderCode());
        tx.setAmount(order.getTotal());
        tx.setStatus("PENDING");
        tx.setCheckoutUrl(result.getCheckoutUrl());
        tx.setPaymentLinkId(result.getPaymentLinkId());
        txRepo.save(tx);

        // clear cart
        cartService.clearCart(cart.getUser().getId());

        return result.getCheckoutUrl();
    }

    @Override
    public List<OrderModel> getAllOrders() {
        return orderRepo.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<OrderModel> getOrdersByUserId(Long userId) {
        return orderRepo.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public String checkoutCustomProduct(CheckoutDto dto) throws Exception {
        UserModel user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        ProductModel product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        OrderModel order = new OrderModel();
        order.setUser(user);
        order.setNote(dto.getNote());
        order.setOrderCode(String.valueOf(System.currentTimeMillis() / 1000));
        order.setStatus("PENDING_APPROVED");
        order.setShippingAddress(dto.getShippingAddress());
        order.setPhoneNumber(dto.getPhoneNumber());
        order.addItem(OrderItemModel.of(
                product.getId(),
                product.getName(),
                product.getImages(),
                product.getPrice(),
                dto.getQuantity()
        ));
        order.recalcTotal();
        orderRepo.save(order);

        return "";
    }
}

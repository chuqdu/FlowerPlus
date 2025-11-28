package base.api.service.impl;

import base.api.dto.request.AddTransactionToOrderDto;
import base.api.dto.request.CheckoutDto;
import base.api.dto.request.OrderDto;
import base.api.entity.*;
import base.api.enums.DeliveryStep;
import base.api.repository.*;
import base.api.service.ICartService;
import base.api.service.IDeliveryStatusService;
import base.api.service.IOrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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



    @Autowired private IDeliveryStatusService deliveryStatusService;

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
        order.setRequestDeliveryTime(dto.getRequestDeliveryTime());
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

        deliveryStatusService.setCurrentStepCascading(
                order.getId(),
                DeliveryStep.PENDING_CONFIRMATION,
                "",
                null,
                null,
                dto.getUserId()
        );

        // Gọi PayOS
        long amount = (long) order.getTotal();
        long orderCode = System.currentTimeMillis() / 1000;

        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(orderCode)
                        .amount(amount)
                        .description("Thanh toan")
                        .returnUrl(dto.getReturnUrl())
                        .cancelUrl(dto.getCancelUrl())
                        .build();

        CreatePaymentLinkResponse result = payOS.paymentRequests().create(paymentData);
        // Lưu transaction
        TransactionModel tx = new TransactionModel();
        tx.setOrder(order);
        tx.setOrderCode(order.getOrderCode());
        tx.setAmount(order.getTotal());
        tx.setStatus("PENDING");
        tx.setCheckoutUrl(result.getCheckoutUrl());
        tx.setPaymentLinkId(result.getPaymentLinkId());
        txRepo.save(tx);

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
        order.setRequestDeliveryTime(dto.getRequestDeliveryTime());
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
        deliveryStatusService.setCurrentStepCascading(
                order.getId(),
                DeliveryStep.PENDING_CONFIRMATION,
                "",
                "",
                "",
                dto.getUserId()
        );


        return "";
    }

    @Override
    public String addPaymentToOrder(AddTransactionToOrderDto dto) throws Exception {
        OrderModel order = orderRepo.findById(dto.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        long amount = (long) dto.getAmount();
        Long transactionCode = System.currentTimeMillis() / 1000;
        long expiredAt = Instant.now().getEpochSecond() + 5 * 60;

        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(transactionCode)
                        .amount(amount)
                        .expiredAt(expiredAt)
                        .description("Thanh toan 3 ngay")
                        .returnUrl(dto.getReturnUrl())
                        .cancelUrl(dto.getCancelUrl())
                        .build();

        CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

        // Lưu transaction
        TransactionModel tx = new TransactionModel();
        tx.setOrder(order);
        tx.setOrderCode(String.valueOf(transactionCode));
        tx.setAmount(amount);
        tx.setStatus("PENDING");
        tx.setCheckoutUrl(response.getCheckoutUrl());
        tx.setPaymentLinkId(response.getPaymentLinkId());

        txRepo.save(tx);

        order.setTotal(amount);

        orderRepo.save(order);
        return response.getCheckoutUrl();
    }
}

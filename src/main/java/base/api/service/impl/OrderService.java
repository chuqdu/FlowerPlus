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
import base.api.service.IVoucherService;
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
    @Autowired private IVoucherService voucherService;
    @Autowired private IVoucherRepository voucherRepo;

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
        // Áp dụng voucher nếu có
        if (dto.getVoucherCode() != null && !dto.getVoucherCode().isBlank()) {
            var validate = voucherService.validateForCart(dto.getUserId(), dto.getVoucherCode());
            if (validate.isValid()) {
                order.setVoucherCode(dto.getVoucherCode());
                order.setDiscountAmount(validate.getDiscountAmount());
                voucherRepo.findByCodeIgnoreCase(dto.getVoucherCode()).ifPresent(v -> {
                    order.setVoucher(v);
                    v.setUsedCount((v.getUsedCount() == null ? 0 : v.getUsedCount()) + 1);
                    voucherRepo.save(v);
                });
            }
        }
        order.recalcTotal();
        orderRepo.save(order);

        deliveryStatusService.setCurrentStepCascading(
                order.getId(),
                DeliveryStep.PENDING_CONFIRMATION,
                "Vui lòng thanh toán để xác nhận đơn hàng.",
                null,
                null,
                dto.getUserId()
        );

        // Gọi PayOS
        long amount = (long) order.getTotal();
        long transactionCode = System.currentTimeMillis() / 1000;

        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(transactionCode)
                        .amount(amount)
                        .description("Thanh toan")
                        .returnUrl(dto.getReturnUrl())
                        .cancelUrl(dto.getCancelUrl())
                        .build();

        CreatePaymentLinkResponse result = payOS.paymentRequests().create(paymentData);
        // Lưu transaction
        TransactionModel tx = new TransactionModel();
        tx.setOrder(order);
        tx.setOrderCode(String.valueOf(transactionCode));
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

        // Áp dụng voucher nếu có
        if (dto.getVoucherCode() != null && !dto.getVoucherCode().isBlank()) {
            base.api.dto.request.voucher.ValidateVoucherRequestItem x = new base.api.dto.request.voucher.ValidateVoucherRequestItem();
            x.setProductId(product.getId());
            x.setUnitPrice(product.getPrice());
            x.setQuantity(dto.getQuantity());
            var validate = voucherService.validateForItems(dto.getVoucherCode(), java.util.List.of(x));
            if (validate.isValid()) {
                order.setVoucherCode(dto.getVoucherCode());
                order.setDiscountAmount(validate.getDiscountAmount());
                voucherRepo.findByCodeIgnoreCase(dto.getVoucherCode()).ifPresent(v -> {
                    order.setVoucher(v);
                    v.setUsedCount((v.getUsedCount() == null ? 0 : v.getUsedCount()) + 1);
                    voucherRepo.save(v);
                });
            }
        }

        order.recalcTotal();
        orderRepo.save(order);

        deliveryStatusService.setCurrentStepCascading(
                order.getId(),
                DeliveryStep.PENDING_CONFIRMATION,
                "Vui lòng chờ xác nhận đơn hàng. Link thanh toán sẽ có sau khi đơn hàng được xác nhận.",
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
        long expiredAt = Instant.now().getEpochSecond() + 5 * 60;

        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(Long.parseLong(order.getOrderCode()))
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
        tx.setOrderCode(order.getOrderCode());
        tx.setAmount(amount);
        tx.setStatus("PENDING");
        tx.setCheckoutUrl(response.getCheckoutUrl());
        tx.setPaymentLinkId(response.getPaymentLinkId());

        txRepo.save(tx);

        order.setTotal(amount);

        orderRepo.save(order);
        return response.getCheckoutUrl();
    }

    @Transactional
    @Override
    public void handlePaymentSuccess(String orderCode) throws Exception {
        // Tìm transaction theo orderCode
        TransactionModel tx = txRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        // Cập nhật trạng thái transaction
        tx.setStatus("SUCCESS");
        txRepo.save(tx);

        // Cập nhật delivery step sang PREPARING
        OrderModel order = tx.getOrder();
        deliveryStatusService.setCurrentStepCascading(
                order.getId(),
                DeliveryStep.PREPARING,
                "Thanh toán thành công, hệ thống đang chuẩn bị đơn hàng",
                null,
                null,
                order.getUser().getId()
        );
    }
}

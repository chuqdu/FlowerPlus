package base.api.service.impl;

import base.api.dto.request.AddTransactionToOrderDto;
import base.api.dto.request.CheckoutDto;
import base.api.entity.*;
import base.api.enums.DeliveryStep;
import base.api.enums.NotificationType;
import base.api.enums.UserRole;
import base.api.repository.*;
import base.api.service.ICartService;
import base.api.service.IDeliveryStatusService;
import base.api.service.INotificationDbService;
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
import java.util.List;
import java.util.Optional;

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
    private base.api.repository.IRefundRequestRepository refundRequestRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ITransactionRepository txRepo;

    @Autowired
    private base.api.config.EmailService emailService;

    @Autowired
    private INotificationDbService notificationDbService;

    @Autowired
    private IDeliveryStatusService deliveryStatusService;
    @Autowired
    private IVoucherService voucherService;
    @Autowired
    private IVoucherRepository voucherRepo;
    @Autowired
    private base.api.repository.IDeliveryAddressRepository deliveryAddressRepository;

    @Transactional
    @Override
    public String checkout(CheckoutDto dto) throws Exception {
        CartModel cart = cartRepo.findByUser_Id(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        if (cart.getCartItems().isEmpty())
            throw new IllegalStateException("Cart is empty");

        // Tạo order
        OrderModel order = new OrderModel();
        order.setUser(cart.getUser());
        order.setOrderCode(String.valueOf(System.currentTimeMillis() / 1000));
        order.setRequestDeliveryTime(dto.getRequestDeliveryTime());
        order.setShippingAddress(dto.getShippingAddress());
        order.setPhoneNumber(dto.getPhoneNumber());
        order.setRecipientName(dto.getRecipientName());
        order.setNote(dto.getNote());

        for (CartItemModel ci : cart.getCartItems()) {
            order.addItem(OrderItemModel.of(
                    ci.getProductId(),
                    ci.getProductName(),
                    ci.getProductImage(),
                    ci.getUnitPrice(),
                    ci.getQuantity()));
        }
        // Áp dụng voucher nếu có
        if (dto.getVoucherCode() != null && !dto.getVoucherCode().isBlank()) {
            var validate = voucherService.validateForCart(dto.getUserId(), dto.getVoucherCode());
            if (validate.isValid()) {
                // Apply voucher với kiểm tra usage limit thread-safe
                VoucherModel voucher = applyVoucherToOrder(order, dto.getVoucherCode(), validate.getDiscountAmount());
                if (voucher == null) {
                    throw new IllegalStateException("Voucher đã hết lượt sử dụng");
                }
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
                dto.getUserId());

        // Gọi PayOS
        long amount = (long) order.getTotal();
        long transactionCode = System.currentTimeMillis() / 1000;

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
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
    public List<OrderModel> getOrdersByUserIdAndVoucherId(Long userId, Long voucherId) {
        return orderRepo.findByUser_IdAndVoucher_IdOrderByCreatedAtDesc(userId, voucherId);
    }

    @Override
    public String checkoutCustomProduct(CheckoutDto dto) throws Exception {
        UserModel user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        ProductModel product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        OrderModel order = new OrderModel();
        order.setUser(user);
        order.setNote(dto.getNote());
        order.setOrderCode(String.valueOf(System.currentTimeMillis() / 1000));
        order.setRequestDeliveryTime(dto.getRequestDeliveryTime());
        order.setShippingAddress(dto.getShippingAddress());
        order.setPhoneNumber(dto.getPhoneNumber());
        order.setRecipientName(dto.getRecipientName());
        order.addItem(OrderItemModel.of(
                product.getId(),
                product.getName(),
                product.getImages(),
                product.getPrice(),
                dto.getQuantity()));

        // Áp dụng voucher nếu có
        if (dto.getVoucherCode() != null && !dto.getVoucherCode().isBlank()) {
            base.api.dto.request.voucher.ValidateVoucherRequestItem x = new base.api.dto.request.voucher.ValidateVoucherRequestItem();
            x.setProductId(product.getId());
            x.setUnitPrice(product.getPrice());
            x.setQuantity(dto.getQuantity());
            var validate = voucherService.validateForItems(dto.getVoucherCode(), java.util.List.of(x));
            if (validate.isValid()) {
                // Apply voucher với kiểm tra usage limit thread-safe
                VoucherModel voucher = applyVoucherToOrder(order, dto.getVoucherCode(), validate.getDiscountAmount());
                if (voucher == null) {
                    throw new IllegalStateException("Voucher đã hết lượt sử dụng");
                }
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
                dto.getUserId());

        return "";
    }

    @Override
    public String addPaymentToOrder(AddTransactionToOrderDto dto) throws Exception {
        OrderModel order = orderRepo.findById(dto.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        long amount = (long) dto.getAmount();
        long expiredAt = Instant.now().getEpochSecond() + 5 * 60;

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
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
                order.getUser().getId());

        // Giảm số lượng hàng hóa (stock) khi thanh toán thành công
        for (OrderItemModel orderItem : order.getItems()) {
            ProductModel product = productRepository.findById(orderItem.getProductId())
                    .orElse(null);

            if (product != null && product.getStock() != null) {
                int newStock = product.getStock() - orderItem.getQuantity();
                if (newStock < 0) {
                    newStock = 0; // Đảm bảo stock không âm
                    order.setNote(order.getNote() + ".Cảnh báo: Sản phẩm " + product.getName()
                            + " đã hết hàng khi thanh toán.");
                }
                product.setStock(newStock);
                productRepository.save(product);
            }
        }

        // Gửi email thông báo thanh toán thành công
        sendPaymentSuccessEmailToCustomer(order);
        sendNewOrderNotificationToShopOwners(order);
    }

    /**
     * Gửi email thông báo thanh toán thành công cho customer
     */
    private void sendPaymentSuccessEmailToCustomer(OrderModel order) {
        try {
            UserModel user = order.getUser();
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                    (user.getLastName() != null ? " " + user.getLastName() : "");
            if (fullName.trim().isEmpty()) {
                fullName = user.getUserName();
            }

            String formattedAmount = String.format("%,.0f", order.getTotal());
            String subject = "✅ Thanh toán thành công - Đơn hàng #" + order.getOrderCode();

            // Tạo danh sách sản phẩm
            StringBuilder itemsHtml = new StringBuilder();
            for (OrderItemModel item : order.getItems()) {
                itemsHtml.append(String.format(
                        "<tr style='border-bottom: 1px solid #e0e0e0;'>" +
                                "<td style='padding: 10px;'>%s</td>" +
                                "<td style='padding: 10px; text-align: center;'>%d</td>" +
                                "<td style='padding: 10px; text-align: right;'>%,.0f VNĐ</td>" +
                                "<td style='padding: 10px; text-align: right; font-weight: bold;'>%,.0f VNĐ</td>" +
                                "</tr>",
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()));
            }

            String body = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                            +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                            "</div>" +
                            "<h2 style='color: #4caf50;'>✅ Thanh toán thành công!</h2>" +
                            "<p>Xin chào <strong>%s</strong>,</p>" +
                            "<p>Cảm ơn bạn đã mua sắm tại FlowerPlus! Đơn hàng <strong>#%s</strong> của bạn đã được thanh toán thành công.</p>"
                            +
                            "<div style='background-color: #e8f5e9; border-left: 4px solid #4caf50; padding: 15px; margin: 20px 0;'>"
                            +
                            "<h3 style='color: #2e7d32; margin-top: 0;'>Thông tin đơn hàng:</h3>" +
                            "<p><strong>Mã đơn hàng:</strong> #%s</p>" +
                            "<p><strong>Tổng tiền:</strong> %s VNĐ</p>" +
                            "<p><strong>Địa chỉ giao hàng:</strong> %s</p>" +
                            "<p><strong>Người nhận:</strong> %s</p>" +
                            "<p><strong>Số điện thoại:</strong> %s</p>" +
                            "%s" +
                            "</div>" +
                            "<div style='margin: 20px 0;'>" +
                            "<h3 style='color: #333; margin-bottom: 15px;'>Danh sách sản phẩm:</h3>" +
                            "<table style='width: 100%%; border-collapse: collapse;'>" +
                            "<thead>" +
                            "<tr style='background-color: #f5f5f5; border-bottom: 2px solid #e0e0e0;'>" +
                            "<th style='padding: 10px; text-align: left;'>Sản phẩm</th>" +
                            "<th style='padding: 10px; text-align: center;'>Số lượng</th>" +
                            "<th style='padding: 10px; text-align: right;'>Đơn giá</th>" +
                            "<th style='padding: 10px; text-align: right;'>Thành tiền</th>" +
                            "</tr>" +
                            "</thead>" +
                            "<tbody>%s</tbody>" +
                            "</table>" +
                            "</div>" +
                            "%s" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<a href='https://flowerplus.site/profile' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Xem chi tiết đơn hàng</a>"
                            +
                            "</div>" +
                            "<p style='color: #666; font-size: 14px;'>Chúng tôi sẽ chuẩn bị và giao hàng cho bạn trong thời gian sớm nhất. Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi.</p>"
                            +
                            "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                            "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>"
                            +
                            "</div>" +
                            "</body>" +
                            "</html>",
                    fullName,
                    order.getOrderCode(),
                    order.getOrderCode(),
                    formattedAmount,
                    order.getShippingAddress() != null ? order.getShippingAddress() : "Chưa có địa chỉ",
                    order.getRecipientName() != null ? order.getRecipientName() : fullName,
                    order.getPhoneNumber() != null ? order.getPhoneNumber() : "Chưa có",
                    order.getRequestDeliveryTime() != null ? "<p><strong>Thời gian yêu cầu giao hàng:</strong> " +
                            order.getRequestDeliveryTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            +
                            "</p>" : "",
                    itemsHtml.toString(),
                    order.getDiscountAmount() > 0 ? String.format(
                            "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>"
                                    +
                                    "<p><strong>Mã giảm giá:</strong> %s</p>" +
                                    "<p><strong>Giảm giá:</strong> %,.0f VNĐ</p>" +
                                    "</div>",
                            order.getVoucherCode() != null ? order.getVoucherCode() : "",
                            order.getDiscountAmount()) : "");

            emailService.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send payment success email to customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi email thông báo đơn hàng mới cho tất cả shop owners, staff và admin
     */
    private void sendNewOrderNotificationToShopOwners(OrderModel order) {
        try {
            // Lấy danh sách tất cả shop owners, staff và admin
            List<UserModel> shopOwners = userRepository.findByRole(UserRole.SHOP_OWNER);
            List<UserModel> staffUsers = userRepository.findByRole(UserRole.STAFF);
            List<UserModel> adminUsers = userRepository.findByRole(UserRole.ADMIN);

            // Gộp tất cả users cần nhận thông báo
            List<UserModel> allRecipients = new java.util.ArrayList<>();
            allRecipients.addAll(shopOwners);
            allRecipients.addAll(staffUsers);
            allRecipients.addAll(adminUsers);

            if (allRecipients.isEmpty()) {
                return; // Không có ai cần nhận thông báo
            }

            UserModel customer = order.getUser();
            String customerFullName = (customer.getFirstName() != null ? customer.getFirstName() : "") +
                    (customer.getLastName() != null ? " " + customer.getLastName() : "");
            if (customerFullName.trim().isEmpty()) {
                customerFullName = customer.getUserName();
            }

            String formattedAmount = String.format("%,.0f", order.getTotal());
            String subject = "🛒 Đơn hàng mới - #" + order.getOrderCode() + " cần chuẩn bị";

            // Tạo danh sách sản phẩm
            StringBuilder itemsHtml = new StringBuilder();
            for (OrderItemModel item : order.getItems()) {
                itemsHtml.append(String.format(
                        "<tr style='border-bottom: 1px solid #e0e0e0;'>" +
                                "<td style='padding: 10px;'>%s</td>" +
                                "<td style='padding: 10px; text-align: center;'>%d</td>" +
                                "<td style='padding: 10px; text-align: right;'>%,.0f VNĐ</td>" +
                                "<td style='padding: 10px; text-align: right; font-weight: bold;'>%,.0f VNĐ</td>" +
                                "</tr>",
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getLineTotal()));
            }

            String body = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                            +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                            "</div>" +
                            "<h2 style='color: #2196f3;'>🛒 Đơn hàng mới cần chuẩn bị</h2>" +
                            "<p>Xin chào Shop Owner,</p>" +
                            "<p>Bạn có một đơn hàng mới <strong>#%s</strong> đã được thanh toán thành công và cần được chuẩn bị.</p>"
                            +
                            "<div style='background-color: #e3f2fd; border-left: 4px solid #2196f3; padding: 15px; margin: 20px 0;'>"
                            +
                            "<h3 style='color: #1565c0; margin-top: 0;'>Thông tin khách hàng:</h3>" +
                            "<p><strong>Tên khách hàng:</strong> %s</p>" +
                            "<p><strong>Số điện thoại:</strong> %s</p>" +
                            "<p><strong>Email:</strong> %s</p>" +
                            "</div>" +
                            "<div style='background-color: #f9f9f9; border-left: 4px solid #666; padding: 15px; margin: 20px 0;'>"
                            +
                            "<h3 style='color: #333; margin-top: 0;'>Thông tin đơn hàng:</h3>" +
                            "<p><strong>Mã đơn hàng:</strong> #%s</p>" +
                            "<p><strong>Tổng tiền:</strong> %s VNĐ</p>" +
                            "<p><strong>Địa chỉ giao hàng:</strong> %s</p>" +
                            "<p><strong>Người nhận:</strong> %s</p>" +
                            "%s" +
                            "%s" +
                            "</div>" +
                            "<div style='margin: 20px 0;'>" +
                            "<h3 style='color: #333; margin-bottom: 15px;'>Danh sách sản phẩm:</h3>" +
                            "<table style='width: 100%%; border-collapse: collapse;'>" +
                            "<thead>" +
                            "<tr style='background-color: #f5f5f5; border-bottom: 2px solid #e0e0e0;'>" +
                            "<th style='padding: 10px; text-align: left;'>Sản phẩm</th>" +
                            "<th style='padding: 10px; text-align: center;'>Số lượng</th>" +
                            "<th style='padding: 10px; text-align: right;'>Đơn giá</th>" +
                            "<th style='padding: 10px; text-align: right;'>Thành tiền</th>" +
                            "</tr>" +
                            "</thead>" +
                            "<tbody>%s</tbody>" +
                            "</table>" +
                            "</div>" +
                            "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>"
                            +
                            "<h3 style='color: #856404; margin-top: 0;'>⚠️ Lưu ý</h3>" +
                            "<p style='margin: 0;'>Vui lòng chuẩn bị đơn hàng và cập nhật trạng thái trong hệ thống quản lý đơn hàng.</p>"
                            +
                            "</div>" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<a href='https://flowerplus.site/admin/orders' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Quản lý đơn hàng</a>"
                            +
                            "</div>" +
                            "<p style='color: #666; font-size: 14px;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>"
                            +
                            "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                            "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>"
                            +
                            "</div>" +
                            "</body>" +
                            "</html>",
                    order.getOrderCode(),
                    customerFullName,
                    order.getPhoneNumber() != null ? order.getPhoneNumber()
                            : (customer.getPhone() != null ? customer.getPhone() : "Chưa có"),
                    customer.getEmail() != null ? customer.getEmail() : "Chưa có",
                    order.getOrderCode(),
                    formattedAmount,
                    order.getShippingAddress() != null ? order.getShippingAddress() : "Chưa có địa chỉ",
                    order.getRecipientName() != null ? order.getRecipientName() : customerFullName,
                    order.getRequestDeliveryTime() != null ? "<p><strong>Thời gian yêu cầu giao hàng:</strong> " +
                            order.getRequestDeliveryTime()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            +
                            "</p>" : "",
                    order.getNote() != null && !order.getNote().isEmpty()
                            ? "<p><strong>Ghi chú:</strong> " + order.getNote() + "</p>"
                            : "",
                    itemsHtml.toString());

            // Gửi email và tạo notification cho tất cả shop owners, staff và admin
            for (UserModel recipient : allRecipients) {
                if (recipient.getEmail() != null && !recipient.getEmail().isEmpty()) {
                    try {
                        emailService.sendHtmlEmail(recipient.getEmail(), subject, body);
                    } catch (Exception e) {
                        System.err.println("Failed to send new order notification to " + recipient.getEmail()
                                + ": " + e.getMessage());
                    }
                }

                // Tạo notification trong database
                try {
                    String notificationTitle = "Đơn hàng mới - #" + order.getOrderCode();
                    String notificationMessage = String.format(
                            "Bạn có đơn hàng mới #%s từ khách hàng %s với tổng tiền %s VNĐ. Vui lòng chuẩn bị đơn hàng.",
                            order.getOrderCode(),
                            customerFullName,
                            formattedAmount);
                    notificationDbService.createNotification(
                            recipient.getId(),
                            notificationTitle,
                            notificationMessage,
                            NotificationType.NEW_ORDER,
                            order.getId(),
                            order.getOrderCode());
                } catch (Exception e) {
                    System.err.println("Failed to create notification for user " + recipient.getId() + ": "
                            + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send new order notification to shop owners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional
    @Override
    public void cancelOrder(Long orderId, Long userId, String reason) throws Exception {
        OrderModel order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng"));

        // if (!order.getUser().getId().equals(userId)) {
        // throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này");
        // }

        // Kiểm tra đơn đã hủy chưa
        if (order.isCancelled()) {
            throw new IllegalStateException("Đơn hàng đã được hủy trước đó");
        }

        // Lấy trạng thái hiện tại
        DeliveryStep currentStep = order.getDeliveryStatuses().isEmpty()
                ? null
                : order.getDeliveryStatuses().get(order.getDeliveryStatuses().size() - 1).getStep();

        // Chỉ cho phép hủy nếu đang ở trạng thái PREPARING
        if (currentStep != DeliveryStep.PREPARING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng khi đang ở trạng thái 'Đang chuẩn bị'");
        }

        // Cập nhật trạng thái đơn hàng
        order.setCancelled(true);
        order.setCancelledAt(LocalDateTime.now());
        orderRepo.save(order);

        // Cập nhật delivery status
        deliveryStatusService.setCurrentStepCascading(
                order.getId(),
                DeliveryStep.CANCELLED,
                "Đơn hàng đã bị hủy. Lý do: " + (reason != null ? reason : "Không có lý do"),
                null,
                null,
                userId);

        // Tạo yêu cầu hoàn tiền
        base.api.entity.RefundRequestModel refundRequest = new base.api.entity.RefundRequestModel();
        refundRequest.setOrder(order);
        refundRequest.setUser(order.getUser());
        refundRequest.setRefundAmount(order.getTotal());
        refundRequest.setStatus(base.api.enums.RefundStatus.PENDING);
        refundRequest.setReason(reason);
        refundRequestRepository.save(refundRequest);

        // Gửi email thông báo hủy đơn hàng
        try {
            UserModel user = order.getUser();
            String subject = "Đơn hàng #" + order.getOrderCode() + " đã được hủy 🔔";
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                    (user.getLastName() != null ? " " + user.getLastName() : "");
            if (fullName.trim().isEmpty()) {
                fullName = user.getUserName();
            }

            String formattedAmount = String.format("%,.0f", order.getTotal());

            String body = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                            +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                            "</div>" +
                            "<h2 style='color: #ff5722;'>Đơn hàng đã được hủy</h2>" +
                            "<p>Xin chào <strong>%s</strong>,</p>" +
                            "<p>Đơn hàng <strong>#%s</strong> của bạn đã được hủy thành công.</p>" +
                            "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>"
                            +
                            "<h3 style='color: #856404; margin-top: 0;'>Thông tin đơn hàng:</h3>" +
                            "<p><strong>Mã đơn hàng:</strong> #%s</p>" +
                            "<p><strong>Số tiền:</strong> %s VNĐ</p>" +
                            "<p><strong>Lý do hủy:</strong> %s</p>" +
                            "<p><strong>Thời gian hủy:</strong> %s</p>" +
                            "</div>" +
                            "<div style='background-color: #d1ecf1; border-left: 4px solid #0dcaf0; padding: 15px; margin: 20px 0;'>"
                            +
                            "<h3 style='color: #055160; margin-top: 0;'>📋 Yêu cầu hoàn tiền</h3>" +
                            "<p style='margin: 0;'>Yêu cầu hoàn tiền đã được tạo tự động. Chúng tôi sẽ xử lý và hoàn tiền cho bạn trong thời gian sớm nhất.</p>"
                            +
                            "<p style='margin: 10px 0 0 0;'>Bạn có thể theo dõi trạng thái hoàn tiền trong trang <strong>Cá nhân > Hoàn tiền</strong>.</p>"
                            +
                            "</div>" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<a href='https://flowerplus.site/profile' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Xem trạng thái hoàn tiền</a>"
                            +
                            "</div>" +
                            "<p style='color: #666; font-size: 14px;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>"
                            +
                            "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                            "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>"
                            +
                            "</div>" +
                            "</body>" +
                            "</html>",
                    fullName,
                    order.getOrderCode(),
                    order.getOrderCode(),
                    formattedAmount,
                    reason != null ? reason : "Không có lý do",
                    java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            emailService.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send order cancellation email: " + e.getMessage());
        }

        // Gửi notification cho shop owners về việc đơn hàng bị hủy
        sendOrderCancellationNotificationToShopOwners(order, userId, reason);
    }

    /**
     * Gửi notification cho shop owners, staff và admin về việc đơn hàng bị hủy
     */
    private void sendOrderCancellationNotificationToShopOwners(OrderModel order, Long cancelledByUserId,
            String reason) {
        try {
            // Lấy danh sách tất cả shop owners, staff và admin
            List<UserModel> shopOwners = userRepository.findByRole(UserRole.SHOP_OWNER);
            List<UserModel> staffUsers = userRepository.findByRole(UserRole.STAFF);
            List<UserModel> adminUsers = userRepository.findByRole(UserRole.ADMIN);

            // Gộp tất cả users cần nhận thông báo
            List<UserModel> allRecipients = new java.util.ArrayList<>();
            allRecipients.addAll(shopOwners);
            allRecipients.addAll(staffUsers);
            allRecipients.addAll(adminUsers);

            if (allRecipients.isEmpty()) {
                return;
            }

            // Lấy thông tin user thực hiện hủy
            UserModel cancelledByUser = userRepository.findById(cancelledByUserId)
                    .orElse(null);
            String cancelledByUserName = "Hệ thống";
            if (cancelledByUser != null) {
                String fullName = (cancelledByUser.getFirstName() != null ? cancelledByUser.getFirstName() : "") +
                        (cancelledByUser.getLastName() != null ? " " + cancelledByUser.getLastName() : "");
                if (fullName.trim().isEmpty()) {
                    cancelledByUserName = cancelledByUser.getUserName();
                } else {
                    cancelledByUserName = fullName.trim();
                }
            }

            UserModel customer = order.getUser();
            String customerFullName = (customer.getFirstName() != null ? customer.getFirstName() : "") +
                    (customer.getLastName() != null ? " " + customer.getLastName() : "");
            if (customerFullName.trim().isEmpty()) {
                customerFullName = customer.getUserName();
            }

            String formattedAmount = String.format("%,.0f", order.getTotal());

            // Tạo notification cho mỗi shop owner, staff và admin
            for (UserModel recipient : allRecipients) {
                try {
                    String notificationTitle = "Đơn hàng bị hủy - #" + order.getOrderCode();
                    String notificationMessage = String.format(
                            "Đơn hàng #%s từ khách hàng %s với tổng tiền %s VNĐ đã bị hủy bởi %s. Lý do: %s",
                            order.getOrderCode(),
                            customerFullName,
                            formattedAmount,
                            cancelledByUserName,
                            reason != null ? reason : "Không có lý do");
                    notificationDbService.createNotification(
                            recipient.getId(),
                            notificationTitle,
                            notificationMessage,
                            NotificationType.ORDER_CANCELLED,
                            order.getId(),
                            order.getOrderCode());
                } catch (Exception e) {
                    System.err.println("Failed to create cancellation notification for user " + recipient.getId()
                            + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send order cancellation notification to shop owners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<base.api.dto.response.RefundRequestDto> getAllRefundRequests() {
        List<base.api.entity.RefundRequestModel> refunds = refundRequestRepository.findAllByOrderByRequestedAtDesc();
        return refunds.stream().map(this::mapToDto).toList();
    }

    @Override
    public List<base.api.dto.response.RefundRequestDto> getUserRefundRequests(Long userId) {
        List<base.api.entity.RefundRequestModel> refunds = refundRequestRepository
                .findByUser_IdOrderByRequestedAtDesc(userId);
        return refunds.stream().map(this::mapToDto).toList();
    }

    @Transactional
    @Override
    public void processRefund(Long refundId, Long adminId, base.api.dto.request.ProcessRefundDto dto) throws Exception {
        base.api.entity.RefundRequestModel refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu hoàn tiền"));

        if (refund.getStatus() != base.api.enums.RefundStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu hoàn tiền đã được xử lý");
        }

        UserModel admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy admin"));

        // Cập nhật trạng thái
        if ("COMPLETED".equals(dto.getStatus())) {
            refund.setStatus(base.api.enums.RefundStatus.COMPLETED);
        } else if ("REJECTED".equals(dto.getStatus())) {
            refund.setStatus(base.api.enums.RefundStatus.REJECTED);
        } else {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }

        refund.setAdminNote(dto.getAdminNote());
        refund.setProofImageUrl(dto.getProofImageUrl());
        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(admin);

        refundRequestRepository.save(refund);

        // Gửi email thông báo hoàn tiền
        try {
            UserModel user = refund.getUser();
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                    (user.getLastName() != null ? " " + user.getLastName() : "");
            if (fullName.trim().isEmpty()) {
                fullName = user.getUserName();
            }

            String formattedAmount = String.format("%,.0f", refund.getRefundAmount());
            String statusText = refund.getStatus() == base.api.enums.RefundStatus.COMPLETED ? "đã được chấp nhận"
                    : "đã bị từ chối";
            String statusColor = refund.getStatus() == base.api.enums.RefundStatus.COMPLETED ? "#4caf50" : "#f44336";
            String statusIcon = refund.getStatus() == base.api.enums.RefundStatus.COMPLETED ? "✅" : "❌";

            String subject = statusIcon + " Cập nhật hoàn tiền đơn hàng #" + refund.getOrder().getOrderCode();

            String body = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                            +
                            "<div style='text-align: center; margin-bottom: 30px;'>" +
                            "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                            "</div>" +
                            "<h2 style='color: %s;'>%s Yêu cầu hoàn tiền %s</h2>" +
                            "<p>Xin chào <strong>%s</strong>,</p>" +
                            "<p>Yêu cầu hoàn tiền cho đơn hàng <strong>#%s</strong> của bạn %s.</p>" +
                            "<div style='background-color: #f9f9f9; border-left: 4px solid %s; padding: 15px; margin: 20px 0;'>"
                            +
                            "<h3 style='color: #333; margin-top: 0;'>Thông tin hoàn tiền:</h3>" +
                            "<p><strong>Mã đơn hàng:</strong> #%s</p>" +
                            "<p><strong>Số tiền hoàn:</strong> %s VNĐ</p>" +
                            "<p><strong>Trạng thái:</strong> <span style='color: %s; font-weight: bold;'>%s</span></p>"
                            +
                            "<p><strong>Thời gian xử lý:</strong> %s</p>" +
                            "</div>",
                    statusColor,
                    statusIcon,
                    statusText,
                    fullName,
                    refund.getOrder().getOrderCode(),
                    statusText,
                    statusColor,
                    refund.getOrder().getOrderCode(),
                    formattedAmount,
                    statusColor,
                    refund.getStatus() == base.api.enums.RefundStatus.COMPLETED ? "Đã hoàn tiền" : "Từ chối",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            if (dto.getAdminNote() != null && !dto.getAdminNote().isEmpty()) {
                body += String.format(
                        "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>"
                                +
                                "<h3 style='color: #856404; margin-top: 0;'>💬 Ghi chú từ admin:</h3>" +
                                "<p style='margin: 0;'>%s</p>" +
                                "</div>",
                        dto.getAdminNote());
            }

            if (refund.getStatus() == base.api.enums.RefundStatus.COMPLETED) {
                body += "<div style='background-color: #d1ecf1; border-left: 4px solid #0dcaf0; padding: 15px; margin: 20px 0;'>"
                        +
                        "<h3 style='color: #055160; margin-top: 0;'>💰 Thông tin hoàn tiền</h3>" +
                        "<p style='margin: 0;'>Số tiền đã được hoàn vào tài khoản của bạn. Vui lòng kiểm tra tài khoản ngân hàng.</p>"
                        +
                        "</div>";
            } else {
                body += "<div style='background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0;'>"
                        +
                        "<h3 style='color: #721c24; margin-top: 0;'>⚠️ Yêu cầu bị từ chối</h3>" +
                        "<p style='margin: 0;'>Yêu cầu hoàn tiền của bạn đã bị từ chối. Vui lòng xem ghi chú từ admin để biết thêm chi tiết.</p>"
                        +
                        "</div>";
            }

            body += "<div style='text-align: center; margin: 30px 0;'>" +
                    "<a href='http://localhost:3000/profile' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Xem chi tiết</a>"
                    +
                    "</div>" +
                    "<p style='color: #666; font-size: 14px;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>"
                    +
                    "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                    "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>"
                    +
                    "</div>" +
                    "</body>" +
                    "</html>";

            emailService.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send refund notification email: " + e.getMessage());
        }
    }

    private base.api.dto.response.RefundRequestDto mapToDto(base.api.entity.RefundRequestModel refund) {
        base.api.dto.response.RefundRequestDto dto = new base.api.dto.response.RefundRequestDto();
        dto.setId(refund.getId());
        dto.setOrderId(refund.getOrder().getId());
        dto.setOrderCode(refund.getOrder().getOrderCode());
        dto.setUserId(refund.getUser().getId());
        dto.setUserName(refund.getUser().getFirstName() + " " + refund.getUser().getLastName());
        dto.setUserEmail(refund.getUser().getEmail());
        dto.setRefundAmount(refund.getRefundAmount());
        dto.setStatus(refund.getStatus());
        dto.setReason(refund.getReason());
        dto.setAdminNote(refund.getAdminNote());
        dto.setProofImageUrl(refund.getProofImageUrl());
        dto.setRequestedAt(refund.getRequestedAt());
        dto.setProcessedAt(refund.getProcessedAt());
        if (refund.getProcessedBy() != null) {
            dto.setProcessedByName(
                    refund.getProcessedBy().getFirstName() + " " + refund.getProcessedBy().getLastName());
        }
        return dto;
    }

    @Override
    @Transactional
    public void updateRequestDeliveryTime(Long orderId, LocalDateTime requestDeliveryTime) throws Exception {
        OrderModel order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        order.setRequestDeliveryTime(requestDeliveryTime);
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public String createOrderForAi(base.api.dto.request.AiCreateOrderDto dto) throws Exception {
        // Lấy thông tin user
        UserModel user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getUserId()));

        // Lấy thông tin product
        ProductModel product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + dto.getProductId()));

        // Lấy địa chỉ mặc định của user
        DeliveryAddressModel defaultAddress = deliveryAddressRepository
                .findByUserIdAndIsDefaultTrue(dto.getUserId())
                .stream()
                .findFirst()
                .orElse(null);

        // Tạo order
        OrderModel order = new OrderModel();
        order.setUser(user);
        order.setOrderCode(String.valueOf(System.currentTimeMillis() / 1000));
        order.setNote("Đơn hàng được tạo tự động bởi AI");

        // Sử dụng thông tin từ địa chỉ mặc định nếu có, nếu không dùng thông tin từ
        // user
        if (defaultAddress != null) {
            order.setShippingAddress(defaultAddress.getAddress());
            order.setPhoneNumber(defaultAddress.getPhoneNumber());
            order.setRecipientName(defaultAddress.getRecipientName());
        } else {
            // Fallback: sử dụng thông tin từ user
            order.setShippingAddress("Chưa có địa chỉ");
            order.setPhoneNumber(user.getPhone() != null ? user.getPhone() : "");
            String recipientName = (user.getFirstName() != null ? user.getFirstName() : "") +
                    " " +
                    (user.getLastName() != null ? user.getLastName() : "");
            order.setRecipientName(recipientName.trim());
        }

        // Thời gian giao hàng mặc định: 2 ngày sau
        order.setRequestDeliveryTime(LocalDateTime.now().plusDays(2));

        // Thêm sản phẩm vào order
        int quantity = dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1;
        order.addItem(OrderItemModel.of(
                product.getId(),
                product.getName(),
                product.getImages(),
                product.getPrice(),
                quantity));

        // Tính tổng tiền
        order.recalcTotal();
        orderRepo.save(order);

        // Tạo delivery status
        deliveryStatusService.setCurrentStepCascading(
                order.getId(),
                DeliveryStep.PENDING_CONFIRMATION,
                "Đơn hàng được tạo tự động bởi AI. Vui lòng thanh toán để xác nhận đơn hàng.",
                "",
                "",
                dto.getUserId());

        // Tạo payment link
        long amount = (long) order.getTotal();
        long transactionCode = System.currentTimeMillis() / 1000;

        String returnUrl = "https://flowerplus.site/payment/success";
        String cancelUrl = "https://flowerplus.site/payment/failure";

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(transactionCode)
                .amount(amount)
                .description("Thanh toan don hang AI")
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
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

        return result.getCheckoutUrl();
    }

    /**
     * Apply voucher to order với kiểm tra usage limit thread-safe
     * 
     * @param order          Order cần apply voucher
     * @param voucherCode    Mã voucher
     * @param discountAmount Số tiền giảm giá
     * @return VoucherModel nếu apply thành công, null nếu voucher đã hết lượt sử
     *         dụng
     */
    @Transactional
    private synchronized VoucherModel applyVoucherToOrder(OrderModel order, String voucherCode, Double discountAmount) {
        // Reload voucher từ DB để có dữ liệu mới nhất (tránh race condition)
        Optional<VoucherModel> voucherOpt = voucherRepo.findByCodeIgnoreCase(voucherCode);
        if (voucherOpt.isEmpty()) {
            return null;
        }

        VoucherModel voucher = voucherOpt.get();

        // Kiểm tra lại usage limit trước khi tăng usedCount
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null) {
            if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
                // Voucher đã hết lượt sử dụng
                return null;
            }
        }

        // Apply voucher vào order
        order.setVoucherCode(voucherCode);
        order.setDiscountAmount(discountAmount);
        order.setVoucher(voucher);

        // Tăng usedCount
        voucher.setUsedCount((voucher.getUsedCount() == null ? 0 : voucher.getUsedCount()) + 1);
        voucherRepo.save(voucher);

        return voucher;
    }
}

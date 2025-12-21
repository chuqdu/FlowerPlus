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
    private base.api.repository.IRefundRequestRepository refundRequestRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private  ITransactionRepository txRepo;

    @Autowired
    private base.api.config.EmailService emailService;



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
        order.setRecipientName(dto.getRecipientName());
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
        order.setRecipientName(dto.getRecipientName());
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

    @Transactional
    @Override
    public void cancelOrder(Long orderId, Long userId, String reason) throws Exception {
        OrderModel order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền sở hữu
//        if (!order.getUser().getId().equals(userId)) {
//            throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này");
//        }

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

        // Kiểm tra thời gian (chỉ cho phép hủy trong vòng 2 giờ sau khi thanh toán)
        LocalDateTime paymentTime = order.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        long hoursDiff = ChronoUnit.HOURS.between(paymentTime, now);
        
        if (hoursDiff > 2) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng trong vòng 2 giờ sau khi đặt hàng");
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
                userId
        );

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
            if(fullName.trim().isEmpty()) {
                fullName = user.getUserName();
            }

            String formattedAmount = String.format("%,.0f", order.getTotal());
            
            String body = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>" +
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                "</div>" +
                "<h2 style='color: #ff5722;'>Đơn hàng đã được hủy</h2>" +
                "<p>Xin chào <strong>%s</strong>,</p>" +
                "<p>Đơn hàng <strong>#%s</strong> của bạn đã được hủy thành công.</p>" +
                "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                "<h3 style='color: #856404; margin-top: 0;'>Thông tin đơn hàng:</h3>" +
                "<p><strong>Mã đơn hàng:</strong> #%s</p>" +
                "<p><strong>Số tiền:</strong> %s VNĐ</p>" +
                "<p><strong>Lý do hủy:</strong> %s</p>" +
                "<p><strong>Thời gian hủy:</strong> %s</p>" +
                "</div>" +
                "<div style='background-color: #d1ecf1; border-left: 4px solid #0dcaf0; padding: 15px; margin: 20px 0;'>" +
                "<h3 style='color: #055160; margin-top: 0;'>📋 Yêu cầu hoàn tiền</h3>" +
                "<p style='margin: 0;'>Yêu cầu hoàn tiền đã được tạo tự động. Chúng tôi sẽ xử lý và hoàn tiền cho bạn trong thời gian sớm nhất.</p>" +
                "<p style='margin: 10px 0 0 0;'>Bạn có thể theo dõi trạng thái hoàn tiền trong trang <strong>Cá nhân > Hoàn tiền</strong>.</p>" +
                "</div>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='http://localhost:3000/profile' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Xem trạng thái hoàn tiền</a>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>" +
                "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                fullName,
                order.getOrderCode(),
                order.getOrderCode(),
                formattedAmount,
                reason != null ? reason : "Không có lý do",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            
            emailService.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send order cancellation email: " + e.getMessage());
        }
    }

    @Override
    public List<base.api.dto.response.RefundRequestDto> getAllRefundRequests() {
        List<base.api.entity.RefundRequestModel> refunds = refundRequestRepository.findAllByOrderByRequestedAtDesc();
        return refunds.stream().map(this::mapToDto).toList();
    }

    @Override
    public List<base.api.dto.response.RefundRequestDto> getUserRefundRequests(Long userId) {
        List<base.api.entity.RefundRequestModel> refunds = refundRequestRepository.findByUser_IdOrderByRequestedAtDesc(userId);
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
            if(fullName.trim().isEmpty()) {
                fullName = user.getUserName();
            }

            String formattedAmount = String.format("%,.0f", refund.getRefundAmount());
            String statusText = refund.getStatus() == base.api.enums.RefundStatus.COMPLETED ? "đã được chấp nhận" : "đã bị từ chối";
            String statusColor = refund.getStatus() == base.api.enums.RefundStatus.COMPLETED ? "#4caf50" : "#f44336";
            String statusIcon = refund.getStatus() == base.api.enums.RefundStatus.COMPLETED ? "✅" : "❌";
            
            String subject = statusIcon + " Cập nhật hoàn tiền đơn hàng #" + refund.getOrder().getOrderCode();
            
            String body = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>" +
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                "</div>" +
                "<h2 style='color: %s;'>%s Yêu cầu hoàn tiền %s</h2>" +
                "<p>Xin chào <strong>%s</strong>,</p>" +
                "<p>Yêu cầu hoàn tiền cho đơn hàng <strong>#%s</strong> của bạn %s.</p>" +
                "<div style='background-color: #f9f9f9; border-left: 4px solid %s; padding: 15px; margin: 20px 0;'>" +
                "<h3 style='color: #333; margin-top: 0;'>Thông tin hoàn tiền:</h3>" +
                "<p><strong>Mã đơn hàng:</strong> #%s</p>" +
                "<p><strong>Số tiền hoàn:</strong> %s VNĐ</p>" +
                "<p><strong>Trạng thái:</strong> <span style='color: %s; font-weight: bold;'>%s</span></p>" +
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
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            if (dto.getAdminNote() != null && !dto.getAdminNote().isEmpty()) {
                body += String.format(
                    "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                    "<h3 style='color: #856404; margin-top: 0;'>💬 Ghi chú từ admin:</h3>" +
                    "<p style='margin: 0;'>%s</p>" +
                    "</div>",
                    dto.getAdminNote()
                );
            }

            if (refund.getStatus() == base.api.enums.RefundStatus.COMPLETED) {
                body += 
                    "<div style='background-color: #d1ecf1; border-left: 4px solid #0dcaf0; padding: 15px; margin: 20px 0;'>" +
                    "<h3 style='color: #055160; margin-top: 0;'>💰 Thông tin hoàn tiền</h3>" +
                    "<p style='margin: 0;'>Số tiền đã được hoàn vào tài khoản của bạn. Vui lòng kiểm tra tài khoản ngân hàng.</p>" +
                    "</div>";
            } else {
                body += 
                    "<div style='background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0;'>" +
                    "<h3 style='color: #721c24; margin-top: 0;'>⚠️ Yêu cầu bị từ chối</h3>" +
                    "<p style='margin: 0;'>Yêu cầu hoàn tiền của bạn đã bị từ chối. Vui lòng xem ghi chú từ admin để biết thêm chi tiết.</p>" +
                    "</div>";
            }

            body += 
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='http://localhost:3000/profile' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Xem chi tiết</a>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>" +
                "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>" +
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
            dto.setProcessedByName(refund.getProcessedBy().getFirstName() + " " + refund.getProcessedBy().getLastName());
        }
        return dto;
    }
}

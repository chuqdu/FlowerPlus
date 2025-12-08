package base.api.dto.response;

import base.api.enums.RefundStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundRequestDto {
    private Long id;
    private Long orderId;
    private String orderCode;
    private Long userId;
    private String userName;
    private String userEmail;
    private double refundAmount;
    private RefundStatus status;
    private String reason;
    private String adminNote;
    private String proofImageUrl;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String processedByName;
}

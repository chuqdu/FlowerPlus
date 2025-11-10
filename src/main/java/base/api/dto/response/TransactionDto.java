package base.api.dto.response;

import base.api.dto.request.OrderDto;
import base.api.entity.OrderModel;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDto {

    private String orderCode;

    private double amount;

    private String status = "PENDING"; // PENDING | SUCCESS | FAILED | CANCELED | EXPIRED

    private String checkoutUrl;
    private String paymentLinkId;
}

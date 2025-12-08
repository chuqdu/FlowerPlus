package base.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTransactionStatusDto {
    private String status; // PENDING | SUCCESS | FAILED | CANCELED | EXPIRED
}

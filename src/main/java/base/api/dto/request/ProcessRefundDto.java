package base.api.dto.request;

import lombok.Data;

@Data
public class ProcessRefundDto {
    private String status; // COMPLETED hoặc REJECTED
    private String adminNote;
    private String proofImageUrl; // URL ảnh minh chứng
}

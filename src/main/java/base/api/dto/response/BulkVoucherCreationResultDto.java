package base.api.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BulkVoucherCreationResultDto {
    private Integer totalRequested;
    private Integer successCount;
    private Integer failureCount;
    private List<PersonalVoucherResponseDto> successfulVouchers;
    private List<BulkCreationErrorDto> errors;
    
    @Data
    public static class BulkCreationErrorDto {
        private Long userId;
        private String userName;
        private String errorMessage;
        private String errorCode;
    }
}
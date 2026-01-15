package base.api.service;

import base.api.dto.request.voucher.CreatePersonalVoucherDto;
import base.api.dto.request.voucher.CreateBulkPersonalVoucherDto;
import base.api.dto.response.PersonalVoucherResponseDto;
import base.api.dto.response.UserVoucherListDto;
import base.api.dto.response.BulkVoucherCreationResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPersonalVoucherService {
    
    // Single personal voucher creation
    PersonalVoucherResponseDto createPersonalVoucher(CreatePersonalVoucherDto dto, String createdBy);
    
    // Bulk personal voucher creation
    BulkVoucherCreationResultDto createBulkPersonalVouchers(CreateBulkPersonalVoucherDto dto, String createdBy);
    
    // User-facing methods
    List<UserVoucherListDto> getUserVouchers(Long userId);
    Page<UserVoucherListDto> getUserVouchersWithPagination(Long userId, Pageable pageable);
    List<UserVoucherListDto> getActiveUserVouchers(Long userId);
    Long countActiveUserVouchers(Long userId);
    
    // Admin management methods
    Page<PersonalVoucherResponseDto> getAllPersonalVouchers(Pageable pageable);
    Page<PersonalVoucherResponseDto> getPersonalVouchersWithFilters(Long userId, Boolean isUsed, String createdBy, String searchTerm, Pageable pageable);
    List<PersonalVoucherResponseDto> getPersonalVouchersByVoucherId(Long voucherId);
    
    // Voucher lifecycle management
    void deactivatePersonalVoucher(Long userVoucherId, String deactivatedBy);
    void deactivateAllVouchersForUser(Long userId, String deactivatedBy);
    
    // Notification and reminder methods
    void sendVoucherAssignmentNotification(Long userVoucherId);
    void sendExpirationReminders();
    List<PersonalVoucherResponseDto> getVouchersExpiringSoon();
}
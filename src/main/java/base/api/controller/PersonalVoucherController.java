package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.paging.PageResponseDTO;
import base.api.dto.request.voucher.CreatePersonalVoucherDto;
import base.api.dto.request.voucher.CreateBulkPersonalVoucherDto;
import base.api.dto.request.voucher.ValidateVoucherRequest;
import base.api.dto.response.TFUResponse;
import base.api.dto.response.PersonalVoucherResponseDto;
import base.api.dto.response.UserVoucherListDto;
import base.api.dto.response.BulkVoucherCreationResultDto;
import base.api.dto.response.ValidateVoucherResponse;
import base.api.service.IPersonalVoucherService;
import base.api.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal-vouchers")
public class PersonalVoucherController extends BaseAPIController {

    @Autowired
    private IPersonalVoucherService personalVoucherService;
    
    @Autowired
    private IVoucherService voucherService;

    // Admin endpoints for creating and managing personal vouchers
    
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<PersonalVoucherResponseDto>> createPersonalVoucher(@RequestBody CreatePersonalVoucherDto dto) {
        // Get current admin user info for audit trail
        String createdBy = getCurrentUserName(); // We'll need to implement this
        PersonalVoucherResponseDto result = personalVoucherService.createPersonalVoucher(dto, createdBy);
        return success(result, "Personal voucher created successfully");
    }

    @PostMapping("/admin/create-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<BulkVoucherCreationResultDto>> createBulkPersonalVouchers(@RequestBody CreateBulkPersonalVoucherDto dto) {
        String createdBy = getCurrentUserName();
        BulkVoucherCreationResultDto result = personalVoucherService.createBulkPersonalVouchers(dto, createdBy);
        return success(result, "Bulk personal voucher creation completed");
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<PageResponseDTO<PersonalVoucherResponseDto>>> getAllPersonalVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PersonalVoucherResponseDto> result = personalVoucherService.getAllPersonalVouchers(pageable);
        return successPage(result);
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<PageResponseDTO<PersonalVoucherResponseDto>>> searchPersonalVouchers(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isUsed,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PersonalVoucherResponseDto> result = personalVoucherService.getPersonalVouchersWithFilters(userId, isUsed, createdBy, searchTerm, pageable);
        return successPage(result);
    }

    @GetMapping("/admin/by-voucher/{voucherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<List<PersonalVoucherResponseDto>>> getPersonalVouchersByVoucherId(@PathVariable Long voucherId) {
        List<PersonalVoucherResponseDto> result = personalVoucherService.getPersonalVouchersByVoucherId(voucherId);
        return success(result, "Personal vouchers retrieved successfully");
    }

    @DeleteMapping("/admin/{userVoucherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<Void>> deactivatePersonalVoucher(@PathVariable Long userVoucherId) {
        String deactivatedBy = getCurrentUserName();
        personalVoucherService.deactivatePersonalVoucher(userVoucherId, deactivatedBy);
        return success(null, "Personal voucher deactivated successfully");
    }

    @DeleteMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<Void>> deactivateAllVouchersForUser(@PathVariable Long userId) {
        String deactivatedBy = getCurrentUserName();
        personalVoucherService.deactivateAllVouchersForUser(userId, deactivatedBy);
        return success(null, "All personal vouchers for user deactivated successfully");
    }

    @GetMapping("/admin/expiring-soon")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TFUResponse<List<PersonalVoucherResponseDto>>> getVouchersExpiringSoon() {
        List<PersonalVoucherResponseDto> result = personalVoucherService.getVouchersExpiringSoon();
        return success(result, "Expiring vouchers retrieved successfully");
    }

    // User endpoints for viewing personal vouchers

    @GetMapping("/my-vouchers")
    public ResponseEntity<TFUResponse<List<UserVoucherListDto>>> getMyVouchers() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return unauthorized("User not authenticated");
        }
        List<UserVoucherListDto> result = personalVoucherService.getUserVouchers(userId);
        return success(result, "Your vouchers retrieved successfully");
    }

    @GetMapping("/my-vouchers/paginated")
    public ResponseEntity<TFUResponse<PageResponseDTO<UserVoucherListDto>>> getMyVouchersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return unauthorized("User not authenticated");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<UserVoucherListDto> result = personalVoucherService.getUserVouchersWithPagination(userId, pageable);
        return successPage(result);
    }

    @GetMapping("/my-vouchers/active")
    public ResponseEntity<TFUResponse<List<UserVoucherListDto>>> getMyActiveVouchers() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return unauthorized("User not authenticated");
        }
        List<UserVoucherListDto> result = personalVoucherService.getActiveUserVouchers(userId);
        return success(result, "Your active vouchers retrieved successfully");
    }

    @GetMapping("/my-vouchers/count")
    public ResponseEntity<TFUResponse<Long>> getMyActiveVoucherCount() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return unauthorized("User not authenticated");
        }
        Long count = personalVoucherService.countActiveUserVouchers(userId);
        return success(count, "Active voucher count retrieved successfully");
    }

    // Voucher validation endpoints (for checkout process)

    @PostMapping("/validate")
    public ResponseEntity<TFUResponse<ValidateVoucherResponse>> validatePersonalVoucher(@RequestBody ValidateVoucherRequest req) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return unauthorized("User not authenticated");
        }

        ValidateVoucherResponse resp;
        if (req.isUseCurrentUserCart()) {
            resp = voucherService.validatePersonalVoucherForCart(userId, req.getCode());
        } else {
            resp = voucherService.validatePersonalVoucher(userId, req.getCode(), req.getItems());
        }
        return success(resp, "Voucher validation completed");
    }

    @GetMapping("/validate-current-cart")
    public ResponseEntity<TFUResponse<ValidateVoucherResponse>> validatePersonalVoucherForCart(@RequestParam String code) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return unauthorized("User not authenticated");
        }
        ValidateVoucherResponse resp = voucherService.validatePersonalVoucherForCart(userId, code);
        return success(resp, "Voucher validation completed");
    }

    // Helper method to get current user name (we'll need to implement this)
    private String getCurrentUserName() {
        // TODO: Implement method to get current user name from JWT token or user service
        // For now, return a placeholder
        Long userId = getCurrentUserId();
        return userId != null ? "admin_" + userId : "system";
    }
}
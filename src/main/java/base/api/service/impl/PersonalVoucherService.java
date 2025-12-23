package base.api.service.impl;

import base.api.dto.request.voucher.CreatePersonalVoucherDto;
import base.api.dto.request.voucher.CreateBulkPersonalVoucherDto;
import base.api.dto.request.voucher.CreateVoucherDto;
import base.api.dto.response.PersonalVoucherResponseDto;
import base.api.dto.response.UserVoucherListDto;
import base.api.dto.response.BulkVoucherCreationResultDto;
import base.api.entity.UserModel;
import base.api.entity.UserVoucherModel;
import base.api.entity.VoucherModel;
import base.api.repository.IUserRepository;
import base.api.repository.IUserVoucherRepository;
import base.api.repository.IVoucherRepository;
import base.api.service.IPersonalVoucherService;
import base.api.service.IVoucherService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonalVoucherService implements IPersonalVoucherService {

    @Autowired
    private IUserVoucherRepository userVoucherRepo;
    
    @Autowired
    private IVoucherRepository voucherRepo;
    
    @Autowired
    private IUserRepository userRepo;
    
    @Autowired
    private IVoucherService voucherService;
    
    @Autowired
    private base.api.service.INotificationService notificationService;
    
    @Autowired
    private ModelMapper mapper;

    @Override
    @Transactional
    public PersonalVoucherResponseDto createPersonalVoucher(CreatePersonalVoucherDto dto, String createdBy) {
        // Validate target user exists
        UserModel targetUser = userRepo.findById(dto.getTargetUserId())
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));

        // Create voucher using existing service
        CreateVoucherDto voucherDto = new CreateVoucherDto();
        voucherDto.setCode(dto.getCode() != null ? dto.getCode() : generateUniqueVoucherCode());
        voucherDto.setType(dto.getType());
        voucherDto.setPercent(dto.getPercent());
        voucherDto.setAmount(dto.getAmount());
        voucherDto.setMinOrderValue(dto.getMinOrderValue());
        voucherDto.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        voucherDto.setStartsAt(dto.getStartsAt());
        voucherDto.setEndsAt(dto.getEndsAt());
        voucherDto.setUsageLimit(dto.getUsageLimit());
        voucherDto.setApplyAllProducts(dto.getApplyAllProducts());
        voucherDto.setProductIds(dto.getProductIds());

        var voucherResponse = voucherService.create(voucherDto);
        VoucherModel voucher = voucherRepo.findById(voucherResponse.getId())
                .orElseThrow(() -> new EntityNotFoundException("Created voucher not found"));

        // Check if voucher is already assigned to this user
        if (userVoucherRepo.existsByVoucherAndUser(voucher, targetUser)) {
            throw new IllegalArgumentException("Voucher already assigned to this user");
        }

        // Create user voucher assignment
        UserVoucherModel userVoucher = new UserVoucherModel();
        userVoucher.setVoucher(voucher);
        userVoucher.setUser(targetUser);
        userVoucher.setAssignedAt(LocalDateTime.now());
        userVoucher.setCreatedBy(createdBy);
        userVoucher.setIsUsed(false);

        userVoucherRepo.save(userVoucher);

        // Send notification to user
        try {
            notificationService.sendVoucherAssignmentNotification(userVoucher);
        } catch (Exception e) {
            // Log error but don't fail the voucher creation
            // Notification failure should not block voucher creation
        }

        return toPersonalVoucherResponseDto(userVoucher);
    }

    @Override
    @Transactional
    public BulkVoucherCreationResultDto createBulkPersonalVouchers(CreateBulkPersonalVoucherDto dto, String createdBy) {
        BulkVoucherCreationResultDto result = new BulkVoucherCreationResultDto();
        result.setTotalRequested(dto.getTargetUserIds().size());
        result.setSuccessfulVouchers(new ArrayList<>());
        result.setErrors(new ArrayList<>());

        for (Long userId : dto.getTargetUserIds()) {
            try {
                CreatePersonalVoucherDto personalVoucherDto = new CreatePersonalVoucherDto();
                personalVoucherDto.setCode(generateUniqueVoucherCode(dto.getCodePrefix()));
                personalVoucherDto.setType(dto.getType());
                personalVoucherDto.setPercent(dto.getPercent());
                personalVoucherDto.setAmount(dto.getAmount());
                personalVoucherDto.setMinOrderValue(dto.getMinOrderValue());
                personalVoucherDto.setMaxDiscountAmount(dto.getMaxDiscountAmount());
                personalVoucherDto.setStartsAt(dto.getStartsAt());
                personalVoucherDto.setEndsAt(dto.getEndsAt());
                personalVoucherDto.setUsageLimit(dto.getUsageLimit());
                personalVoucherDto.setApplyAllProducts(dto.getApplyAllProducts());
                personalVoucherDto.setProductIds(dto.getProductIds());
                personalVoucherDto.setTargetUserId(userId);
                personalVoucherDto.setDescription(dto.getDescription());

                PersonalVoucherResponseDto voucher = createPersonalVoucher(personalVoucherDto, createdBy);
                result.getSuccessfulVouchers().add(voucher);

            } catch (Exception e) {
                BulkVoucherCreationResultDto.BulkCreationErrorDto error = new BulkVoucherCreationResultDto.BulkCreationErrorDto();
                error.setUserId(userId);
                error.setErrorMessage(e.getMessage());
                error.setErrorCode("CREATION_FAILED");
                
                // Try to get user name for better error reporting
                try {
                    UserModel user = userRepo.findById(userId).orElse(null);
                    if (user != null) {
                        error.setUserName(user.getUserName());
                    }
                } catch (Exception ignored) {
                    // Ignore if we can't get user name
                }
                
                result.getErrors().add(error);
            }
        }

        result.setSuccessCount(result.getSuccessfulVouchers().size());
        result.setFailureCount(result.getErrors().size());

        return result;
    }

    @Override
    public List<UserVoucherListDto> getUserVouchers(Long userId) {
        List<UserVoucherModel> userVouchers = userVoucherRepo.findByUser_IdOrderByAssignedAtDesc(userId);
        return userVouchers.stream()
                .map(this::toUserVoucherListDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserVoucherListDto> getUserVouchersWithPagination(Long userId, Pageable pageable) {
        Page<UserVoucherModel> userVouchers = userVoucherRepo.findByUser_IdOrderByAssignedAtDesc(userId, pageable);
        return userVouchers.map(this::toUserVoucherListDto);
    }

    @Override
    public List<UserVoucherListDto> getActiveUserVouchers(Long userId) {
        List<UserVoucherModel> activeVouchers = userVoucherRepo.findActiveVouchersByUserId(userId, LocalDateTime.now());
        return activeVouchers.stream()
                .map(this::toUserVoucherListDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long countActiveUserVouchers(Long userId) {
        return userVoucherRepo.countActiveVouchersByUserId(userId, LocalDateTime.now());
    }

    @Override
    public Page<PersonalVoucherResponseDto> getAllPersonalVouchers(Pageable pageable) {
        Page<UserVoucherModel> userVouchers = userVoucherRepo.findAllOrderByAssignedAtDesc(pageable);
        return userVouchers.map(this::toPersonalVoucherResponseDto);
    }

    @Override
    public Page<PersonalVoucherResponseDto> getPersonalVouchersWithFilters(Long userId, Boolean isUsed, String createdBy, Pageable pageable) {
        Page<UserVoucherModel> userVouchers = userVoucherRepo.findWithFilters(userId, isUsed, createdBy, pageable);
        return userVouchers.map(this::toPersonalVoucherResponseDto);
    }

    @Override
    public List<PersonalVoucherResponseDto> getPersonalVouchersByVoucherId(Long voucherId) {
        List<UserVoucherModel> userVouchers = userVoucherRepo.findByVoucher_Id(voucherId);
        return userVouchers.stream()
                .map(this::toPersonalVoucherResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deactivatePersonalVoucher(Long userVoucherId, String deactivatedBy) {
        UserVoucherModel userVoucher = userVoucherRepo.findById(userVoucherId)
                .orElseThrow(() -> new EntityNotFoundException("Personal voucher not found"));
        
        if (!userVoucher.getIsUsed()) {
            userVoucher.setIsUsed(true);
            userVoucher.setUsedAt(LocalDateTime.now());
            // Could add a deactivatedBy field if needed
            userVoucherRepo.save(userVoucher);
        }
    }

    @Override
    @Transactional
    public void deactivateAllVouchersForUser(Long userId, String deactivatedBy) {
        List<UserVoucherModel> userVouchers = userVoucherRepo.findByUser_IdOrderByAssignedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        
        for (UserVoucherModel userVoucher : userVouchers) {
            if (!userVoucher.getIsUsed()) {
                userVoucher.setIsUsed(true);
                userVoucher.setUsedAt(now);
                userVoucherRepo.save(userVoucher);
            }
        }
    }

    @Override
    public void sendVoucherAssignmentNotification(Long userVoucherId) {
        UserVoucherModel userVoucher = userVoucherRepo.findById(userVoucherId)
                .orElseThrow(() -> new EntityNotFoundException("Personal voucher not found"));
        
        notificationService.sendVoucherAssignmentNotification(userVoucher);
    }

    @Override
    public void sendExpirationReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<UserVoucherModel> expiringSoon = userVoucherRepo.findVouchersExpiringSoon(now, tomorrow);
        
        for (UserVoucherModel userVoucher : expiringSoon) {
            try {
                notificationService.sendVoucherExpirationReminder(userVoucher);
            } catch (Exception e) {
                // Log error but continue with other vouchers
            }
        }
    }

    @Override
    public List<PersonalVoucherResponseDto> getVouchersExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<UserVoucherModel> expiringSoon = userVoucherRepo.findVouchersExpiringSoon(now, tomorrow);
        return expiringSoon.stream()
                .map(this::toPersonalVoucherResponseDto)
                .collect(Collectors.toList());
    }

    // Helper methods
    private String generateUniqueVoucherCode() {
        return generateUniqueVoucherCode("PV");
    }

    private String generateUniqueVoucherCode(String prefix) {
        String basePrefix = prefix != null ? prefix : "PV";
        String code;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            code = basePrefix + "-" + uuid;
        } while (voucherRepo.findByCodeIgnoreCase(code).isPresent());
        
        return code;
    }

    private PersonalVoucherResponseDto toPersonalVoucherResponseDto(UserVoucherModel userVoucher) {
        PersonalVoucherResponseDto dto = new PersonalVoucherResponseDto();
        
        // UserVoucher assignment info
        dto.setUserVoucherId(userVoucher.getId());
        dto.setUserId(userVoucher.getUser().getId());
        dto.setUserName(userVoucher.getUser().getUserName());
        dto.setUserEmail(userVoucher.getUser().getEmail());
        dto.setAssignedAt(userVoucher.getAssignedAt());
        dto.setIsUsed(userVoucher.getIsUsed());
        dto.setUsedAt(userVoucher.getUsedAt());
        dto.setCreatedBy(userVoucher.getCreatedBy());
        
        // Voucher details
        VoucherModel voucher = userVoucher.getVoucher();
        dto.setVoucherId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setType(voucher.getType());
        dto.setPercent(voucher.getPercent());
        dto.setAmount(voucher.getAmount());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setStartsAt(voucher.getStartsAt());
        dto.setEndsAt(voucher.getEndsAt());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setApplyAllProducts(voucher.getApplyAllProducts());
        
        // Computed fields
        dto.setIsExpired(userVoucher.isExpired());
        dto.setIsActive(userVoucher.isActive());
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null) {
            dto.setRemainingUsage(Math.max(0, voucher.getUsageLimit() - voucher.getUsedCount()));
        }
        
        return dto;
    }

    private UserVoucherListDto toUserVoucherListDto(UserVoucherModel userVoucher) {
        UserVoucherListDto dto = new UserVoucherListDto();
        
        // UserVoucher info
        dto.setUserVoucherId(userVoucher.getId());
        dto.setAssignedAt(userVoucher.getAssignedAt());
        dto.setIsUsed(userVoucher.getIsUsed());
        dto.setUsedAt(userVoucher.getUsedAt());
        
        // Voucher details
        VoucherModel voucher = userVoucher.getVoucher();
        dto.setVoucherId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setType(voucher.getType());
        dto.setPercent(voucher.getPercent());
        dto.setAmount(voucher.getAmount());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setStartsAt(voucher.getStartsAt());
        dto.setEndsAt(voucher.getEndsAt());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setApplyAllProducts(voucher.getApplyAllProducts());
        
        // Status info
        dto.setIsExpired(userVoucher.isExpired());
        dto.setIsActive(userVoucher.isActive());
        
        // Determine status
        if (userVoucher.getIsUsed()) {
            dto.setStatus("USED");
        } else if (userVoucher.isExpired()) {
            dto.setStatus("EXPIRED");
        } else if (voucher.getStartsAt() != null && LocalDateTime.now().isBefore(voucher.getStartsAt())) {
            dto.setStatus("NOT_STARTED");
        } else {
            dto.setStatus("ACTIVE");
        }
        
        // Days until expiry
        if (voucher.getEndsAt() != null && !userVoucher.isExpired()) {
            dto.setDaysUntilExpiry(ChronoUnit.DAYS.between(LocalDateTime.now(), voucher.getEndsAt()));
        }
        
        return dto;
    }
}
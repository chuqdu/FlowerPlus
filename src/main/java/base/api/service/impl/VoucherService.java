package base.api.service.impl;

import base.api.dto.request.voucher.CreateVoucherDto;
import base.api.dto.request.voucher.UpdateVoucherDto;
import base.api.dto.request.voucher.ValidateVoucherRequestItem;
import base.api.dto.response.ValidateVoucherResponse;
import base.api.dto.response.VoucherResponseDto;
import base.api.entity.CartItemModel;
import base.api.entity.CartModel;
import base.api.entity.ProductModel;
import base.api.entity.UserVoucherModel;
import base.api.entity.VoucherModel;
import base.api.enums.VoucherType;
import base.api.repository.ICartRepository;
import base.api.repository.IProductRepository;
import base.api.repository.IUserVoucherRepository;
import base.api.repository.IVoucherRepository;
import base.api.service.IVoucherService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoucherService implements IVoucherService {

    @Autowired
    private IVoucherRepository voucherRepo;
    @Autowired
    private IProductRepository productRepo;
    @Autowired
    private ICartRepository cartRepo;
    @Autowired
    private IUserVoucherRepository userVoucherRepo;
    @Autowired
    private base.api.service.INotificationService notificationService;
    @Autowired
    private ModelMapper mapper;

    private void validateBusiness(VoucherModel v) {
        if (v.getType() == VoucherType.PERCENTAGE) {
            if (v.getPercent() == null)
                throw new IllegalArgumentException("percent is required for percentage voucher");
            if (v.getPercent() <= 0 || v.getPercent() > 100)
                throw new IllegalArgumentException("percent must be in (0,100]");
        } else if (v.getType() == VoucherType.FIXED) {
            if (v.getAmount() == null)
                throw new IllegalArgumentException("amount is required for fixed voucher");
            if (v.getAmount() <= 0)
                throw new IllegalArgumentException("amount must be > 0");
        }
        if (Boolean.FALSE.equals(v.getApplyAllProducts()) && (v.getProducts() == null || v.getProducts().isEmpty())) {
            throw new IllegalArgumentException("productIds required when applyAllProducts = false");
        }
        if (v.getStartsAt() != null && v.getEndsAt() != null && v.getEndsAt().isBefore(v.getStartsAt())) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }
    }

    private VoucherResponseDto toDto(VoucherModel v) {
        VoucherResponseDto dto = mapper.map(v, VoucherResponseDto.class);
        if (v.getProducts() != null) {
            dto.setProductIds(v.getProducts().stream().map(ProductModel::getId).collect(Collectors.toSet()));
        }
        return dto;
    }

    @Transactional
    @Override
    public VoucherResponseDto create(CreateVoucherDto dto) {
        VoucherModel v = new VoucherModel();
        v.setCode(dto.getCode());
        v.setType(dto.getType());
        v.setPercent(dto.getPercent());
        v.setAmount(dto.getAmount());
        v.setMinOrderValue(dto.getMinOrderValue());
        v.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        v.setStartsAt(dto.getStartsAt());
        v.setEndsAt(dto.getEndsAt());
        v.setUsageLimit(dto.getUsageLimit());
        v.setApplyAllProducts(Boolean.TRUE.equals(dto.getApplyAllProducts()));
        if (Boolean.FALSE.equals(v.getApplyAllProducts()) && dto.getProductIds() != null) {
            List<ProductModel> products = productRepo.findAllById(dto.getProductIds());
            v.getProducts().addAll(products);
        }
        validateBusiness(v);
        voucherRepo.save(v);
        return toDto(v);
    }

    @Transactional
    @Override
    public VoucherResponseDto update(Long id, UpdateVoucherDto dto) {
        VoucherModel v = voucherRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
        if (dto.getType() != null)
            v.setType(dto.getType());
        if (dto.getPercent() != null)
            v.setPercent(dto.getPercent());
        if (dto.getAmount() != null)
            v.setAmount(dto.getAmount());
        if (dto.getMinOrderValue() != null)
            v.setMinOrderValue(dto.getMinOrderValue());
        if (dto.getMaxDiscountAmount() != null)
            v.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        if (dto.getStartsAt() != null)
            v.setStartsAt(dto.getStartsAt());
        if (dto.getEndsAt() != null)
            v.setEndsAt(dto.getEndsAt());
        if (dto.getUsageLimit() != null)
            v.setUsageLimit(dto.getUsageLimit());
        if (dto.getApplyAllProducts() != null)
            v.setApplyAllProducts(dto.getApplyAllProducts());
        if (dto.getProductIds() != null) {
            v.getProducts().clear();
            if (!Boolean.TRUE.equals(v.getApplyAllProducts())) {
                List<ProductModel> products = productRepo.findAllById(dto.getProductIds());
                v.getProducts().addAll(products);
            }
        }
        validateBusiness(v);
        voucherRepo.save(v);
        return toDto(v);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        voucherRepo.deleteById(id);
    }

    @Override
    public VoucherResponseDto get(Long id) {
        return voucherRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
    }

    @Override
    public List<VoucherResponseDto> list() {
        return voucherRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ValidateVoucherResponse validateForItems(String code, List<ValidateVoucherRequestItem> items) {
        ValidateVoucherResponse resp = new ValidateVoucherResponse();
        Optional<VoucherModel> opt = voucherRepo.findByCodeIgnoreCase(code);
        if (opt.isEmpty()) {
            resp.setValid(false);
            resp.setMessage("Voucher không tồn tại");
            return resp;
        }
        VoucherModel v = opt.get();
        return validateVoucherLogic(v, items);
    }

    @Override
    public ValidateVoucherResponse validateForCart(Long userId, String code) {
        CartModel cart = cartRepo.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        List<ValidateVoucherRequestItem> items = cart.getCartItems().stream().map(ci -> {
            ValidateVoucherRequestItem x = new ValidateVoucherRequestItem();
            x.setProductId(ci.getProductId());
            x.setUnitPrice(ci.getUnitPrice());
            x.setQuantity(ci.getQuantity());
            return x;
        }).toList();
        return validateForItems(code, items);
    }

    @Override
    public ValidateVoucherResponse validatePersonalVoucher(Long userId, String code, List<ValidateVoucherRequestItem> items) {
        ValidateVoucherResponse resp = new ValidateVoucherResponse();
        
        // First check if this is a personal voucher for the user
        Optional<UserVoucherModel> userVoucherOpt = userVoucherRepo.findByVoucherCodeAndUserId(code, userId);
        if (userVoucherOpt.isEmpty()) {
            // Not a personal voucher for this user, try regular voucher validation
            return validateForItems(code, items);
        }
        
        UserVoucherModel userVoucher = userVoucherOpt.get();
        VoucherModel voucher = userVoucher.getVoucher();
        
        // Check if personal voucher is already used
        if (userVoucher.getIsUsed()) {
            resp.setValid(false);
            resp.setMessage("Voucher cá nhân đã được sử dụng");
            return resp;
        }
        
        // Use the existing validation logic but with ownership check
        resp = validateVoucherLogic(voucher, items);
        
        // If validation passes, mark it as personal voucher
        if (resp.isValid()) {
            resp.setMessage("Voucher cá nhân hợp lệ");
        }
        
        return resp;
    }

    @Override
    public ValidateVoucherResponse validatePersonalVoucherForCart(Long userId, String code) {
        CartModel cart = cartRepo.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        List<ValidateVoucherRequestItem> items = cart.getCartItems().stream().map(ci -> {
            ValidateVoucherRequestItem x = new ValidateVoucherRequestItem();
            x.setProductId(ci.getProductId());
            x.setUnitPrice(ci.getUnitPrice());
            x.setQuantity(ci.getQuantity());
            return x;
        }).toList();
        return validatePersonalVoucher(userId, code, items);
    }

    // Extract validation logic to reuse for both regular and personal vouchers
    private ValidateVoucherResponse validateVoucherLogic(VoucherModel v, List<ValidateVoucherRequestItem> items) {
        ValidateVoucherResponse resp = new ValidateVoucherResponse();
        resp.setCode(v.getCode());
        resp.setType(v.getType());
        resp.setApplyAllProducts(v.getApplyAllProducts());

        // time window
        LocalDateTime now = LocalDateTime.now();
        if (v.getStartsAt() != null && now.isBefore(v.getStartsAt())) {
            resp.setValid(false);
            resp.setMessage("Voucher chưa bắt đầu hiệu lực");
            return resp;
        }
        if (v.getEndsAt() != null && now.isAfter(v.getEndsAt())) {
            resp.setValid(false);
            resp.setMessage("Voucher đã hết hạn");
            return resp;
        }
        if (v.getUsageLimit() != null && v.getUsedCount() != null && v.getUsedCount() >= v.getUsageLimit()) {
            resp.setValid(false);
            resp.setMessage("Voucher đã đạt giới hạn sử dụng");
            return resp;
        }

        Set<Long> allowedProductIds = Boolean.FALSE.equals(v.getApplyAllProducts())
                ? v.getProducts().stream().map(ProductModel::getId).collect(Collectors.toSet())
                : null;

        List<ValidateVoucherRequestItem> applicable = items.stream()
                .filter(it -> allowedProductIds == null || allowedProductIds.contains(it.getProductId()))
                .toList();

        double applicableSubtotal = applicable.stream().mapToDouble(ValidateVoucherRequestItem::getLineTotal).sum();

        if (v.getMinOrderValue() != null && applicableSubtotal < v.getMinOrderValue()) {
            resp.setValid(false);
            resp.setMessage("Chưa đạt giá trị tối thiểu để áp dụng voucher");
            resp.setApplicableSubtotal(applicableSubtotal);
            return resp;
        }

        double discount = 0;
        if (v.getType() == VoucherType.PERCENTAGE) {
            discount = applicableSubtotal * (v.getPercent() / 100.0);
            if (v.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, v.getMaxDiscountAmount());
            }
        } else {
            discount = v.getAmount() != null ? v.getAmount() : 0;
        }
        discount = Math.min(discount, applicableSubtotal);

        double allSubtotal = items.stream().mapToDouble(ValidateVoucherRequestItem::getLineTotal).sum();
        double finalPayable = Math.max(0, allSubtotal - discount);

        resp.setValid(true);
        resp.setMessage("Voucher hợp lệ");
        resp.setDiscountAmount(discount);
        resp.setApplicableSubtotal(applicableSubtotal);
        resp.setFinalPayable(finalPayable);
        if (allowedProductIds != null)
            resp.setAppliedProductIds(allowedProductIds);
        return resp;
    }

    // Thread-safe method to mark personal voucher as used
    @Transactional
    public synchronized void markPersonalVoucherAsUsed(Long userId, String code) {
        Optional<UserVoucherModel> userVoucherOpt = userVoucherRepo.findByVoucherCodeAndUserId(code, userId);
        if (userVoucherOpt.isPresent()) {
            UserVoucherModel userVoucher = userVoucherOpt.get();
            if (!userVoucher.getIsUsed()) {
                userVoucher.markAsUsed();
                userVoucherRepo.save(userVoucher);
                
                // Also increment the voucher's used count
                VoucherModel voucher = userVoucher.getVoucher();
                voucher.setUsedCount((voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) + 1);
                voucherRepo.save(voucher);
                
                // Send usage confirmation notification
                try {
                    notificationService.sendVoucherUsageConfirmation(userVoucher);
                } catch (Exception e) {
                    // Log error but don't fail the voucher usage
                }
            }
        }
    }
}

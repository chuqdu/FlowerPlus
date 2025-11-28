package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.voucher.CreateVoucherDto;
import base.api.dto.request.voucher.UpdateVoucherDto;
import base.api.dto.request.voucher.ValidateVoucherRequest;
import base.api.dto.request.voucher.ValidateVoucherRequestItem;
import base.api.dto.response.TFUResponse;
import base.api.dto.response.ValidateVoucherResponse;
import base.api.dto.response.VoucherResponseDto;
import base.api.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController extends BaseAPIController {

    @Autowired
    private IVoucherService voucherService;

    @PostMapping
    public ResponseEntity<TFUResponse<VoucherResponseDto>> create(@RequestBody CreateVoucherDto dto) {
        return success(voucherService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TFUResponse<VoucherResponseDto>> update(@PathVariable Long id, @RequestBody UpdateVoucherDto dto) {
        return success(voucherService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TFUResponse<Void>> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return success(null, "Deleted");
    }

    @GetMapping
    public ResponseEntity<TFUResponse<List<VoucherResponseDto>>> list() {
        return success(voucherService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TFUResponse<VoucherResponseDto>> get(@PathVariable Long id) {
        return success(voucherService.get(id));
    }

    @PostMapping("/validate")
    public ResponseEntity<TFUResponse<ValidateVoucherResponse>> validate(@RequestBody ValidateVoucherRequest req) {
        ValidateVoucherResponse resp;
        if (req.isUseCurrentUserCart()) {
            Long userId = getCurrentUserId();
            resp = voucherService.validateForCart(userId, req.getCode());
        } else {
            resp = voucherService.validateForItems(req.getCode(), req.getItems());
        }
        return success(resp);
    }

    @GetMapping("/validate-current-cart")
    public ResponseEntity<TFUResponse<ValidateVoucherResponse>> validateCurrentCart(@RequestParam String code) {
        Long userId = getCurrentUserId();
        return success(voucherService.validateForCart(userId, code));
    }
}


package base.api.service;

import base.api.dto.request.voucher.CreateVoucherDto;
import base.api.dto.request.voucher.UpdateVoucherDto;
import base.api.dto.request.voucher.ValidateVoucherRequestItem;
import base.api.dto.response.ValidateVoucherResponse;
import base.api.dto.response.VoucherResponseDto;

import java.util.List;

public interface IVoucherService {
    VoucherResponseDto create(CreateVoucherDto dto);
    VoucherResponseDto update(Long id, UpdateVoucherDto dto);
    void delete(Long id);
    VoucherResponseDto get(Long id);
    List<VoucherResponseDto> list();

    ValidateVoucherResponse validateForItems(String code, List<ValidateVoucherRequestItem> items);
    ValidateVoucherResponse validateForCart(Long userId, String code);
}


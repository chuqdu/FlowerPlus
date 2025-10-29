package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.impl.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<String> uploadSingleImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseDto.fail("Không có file nào được chọn để upload");
        }
        try {
            String imageUrl = imageUploadService.uploadImage(file);
            return ResponseDto.success(imageUrl, "Upload ảnh thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }
}

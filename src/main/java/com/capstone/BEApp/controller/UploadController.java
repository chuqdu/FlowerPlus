package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.impl.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/image")
    public ResponseDto<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageUploadService.uploadImage(file);
            return ResponseDto.success(imageUrl, "Upload ảnh thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

}

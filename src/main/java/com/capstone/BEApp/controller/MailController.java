package com.capstone.BEApp.controller;

import com.capstone.BEApp.dto.common.ResponseDto;
import com.capstone.BEApp.service.impl.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/send")
    public ResponseDto<String> sendMail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body
    ) {
        try {
            mailService.sendSimpleMail(to, subject, body);
            return ResponseDto.success(null, "Gửi mail thành công");
        } catch (Exception e) {
            return ResponseDto.fail("Gửi mail thất bại: " + e.getMessage());
        }
    }
}

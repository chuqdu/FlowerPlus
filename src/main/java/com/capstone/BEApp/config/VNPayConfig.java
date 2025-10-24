package com.capstone.BEApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.tmn_code}")
    public String vnp_TmnCode;

    @Value("${vnpay.hash_secret}")
    public String vnp_HashSecret;

    @Value("${vnpay.api_url}")
    public String vnp_PayUrl;

    @Value("${vnpay.return_url}")
    public String vnp_ReturnUrl;

}

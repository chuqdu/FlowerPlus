package com.capstone.BEApp.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

public class VNPayUtil {

    /**
     * Tạo chữ ký HMAC SHA512 cho dữ liệu gửi đến VNPay.
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) return "";
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi mã hóa HMAC SHA512: " + ex.getMessage(), ex);
        }
    }

    /**
     * Gom tất cả tham số thành chuỗi query string (dùng để hash).
     */
    public static String hashAllFields(Map<String, String> fields) {
        TreeMap<String, String> sorted = new TreeMap<>(fields);
        StringJoiner joiner = new StringJoiner("&");
        sorted.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                joiner.add(key + "=" + value);
            }
        });
        return joiner.toString();
    }

    /**
     * Lấy IP address của client.
     */
    public static String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}

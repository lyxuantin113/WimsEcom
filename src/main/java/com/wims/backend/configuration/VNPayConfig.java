package com.wims.backend.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class VNPayConfig {

    public static String vnp_PayUrl;
    public static String vnp_ReturnUrl;
    public static String vnp_TmnCode;
    public static String vnp_HashSecret;
    public static String vnp_ApiUrl;

    @Value("${vnpay.payurl}")
    public void setVnp_PayUrl(String payUrl) {
        vnp_PayUrl = payUrl;
    }

    @Value("${vnpay.returnurl}")
    public void setVnp_ReturnUrl(String returnUrl) {
        vnp_ReturnUrl = returnUrl;
    }

    @Value("${vnpay.tmncode}")
    public void setVnp_TmnCode(String tmnCode) {
        vnp_TmnCode = tmnCode;
    }

    @Value("${vnpay.hashsecret}")
    public void setVnp_HashSecret(String hashSecret) {
        vnp_HashSecret = hashSecret;
    }

    @Value("${vnpay.apiurl}")
    public void setVnp_ApiUrl(String apiUrl) {
        vnp_ApiUrl = apiUrl;
    }

    // Hàm tiện ích: Hash dữ liệu bằng HMAC SHA512
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    // Hàm tiện ích: Lấy IP của người dùng (VNPay yêu cầu)
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }

    // Hàm tiện ích: Random chuỗi (để tạo mã giao dịch)
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String hashAllFields(Map fields) {
        List fieldNames = new ArrayList(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                sb.append(fieldName);
                sb.append("=");
                // ⚠️ QUAN TRỌNG: Phải Encode dữ liệu (ví dụ: dấu cách -> %20) thì mới khớp Hash của VNPay
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }
}
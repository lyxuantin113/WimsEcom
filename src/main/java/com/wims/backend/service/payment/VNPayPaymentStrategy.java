package com.wims.backend.service.payment;

import com.wims.backend.configuration.VNPayConfig;
import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.VNPayResponse;
import com.wims.backend.entity.Order;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayPaymentStrategy implements PaymentStrategy {

    private final OrderRepository orderRepository;

    @Override
    public String createPaymentUrl(Order order, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(order.getId());
        long amountVal = order.getTotalAmount().longValue() * 100;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountVal));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan thanh cong" + order.getId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        return VNPayConfig.vnp_PayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    @Override
    public ApiResponse<?> handleCallback(Map<String, String> params) {
        String status = params.get("vnp_ResponseCode");
        String vnp_SecureHash = params.get("vnp_SecureHash");

        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = VNPayConfig.hashAllFields(fields);

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(status)) {
                String orderIdStr = params.get("vnp_TxnRef");
                String transactionId = params.get("vnp_TransactionNo");
                String paymentTime = params.get("vnp_PayDate");
                String totalPrice = params.get("vnp_Amount");

                try {
                    Long orderId = Long.parseLong(orderIdStr);
                    Order order = orderRepository.findById(orderId).orElseThrow();
                    BigDecimal vnpAmount = new BigDecimal(totalPrice).divide(new BigDecimal(100));

                    if (order.getTotalAmount().compareTo(vnpAmount) == 0) {
                        order.setStatus(OrderStatus.PAID);
                        order.setPaymentTime(LocalDateTime.now());
                        orderRepository.save(order);

                        return ApiResponse.success(VNPayResponse.builder()
                                .transactionId(transactionId)
                                .orderId(orderIdStr)
                                .paymentTime(paymentTime)
                                .totalPrice(totalPrice)
                                .build()).build();
                    }
                    return ApiResponse.builder().code(7777).message("Số tiền không khớp").build();
                } catch (Exception e) {
                    return ApiResponse.builder().code(7777).message("Lỗi xử lý: " + e.getMessage()).build();
                }
            }
            return ApiResponse.builder().code(7777).message("Giao dịch thất bại").build();
        }
        return ApiResponse.builder().code(7777).message("Chữ ký không hợp lệ").build();
    }

    @Override
    public String getMethodName() {
        return "VNPAY";
    }

    @Override
    public OrderStatus getInitialOrderStatus() {
        return OrderStatus.PENDING_PAYMENT;
    }
}

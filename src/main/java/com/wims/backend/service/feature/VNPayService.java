package com.wims.backend.service.feature;

import com.wims.backend.configuration.VNPayConfig;
import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.VNPayResponse;
import com.wims.backend.entity.Order;
import com.wims.backend.entity.User;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.OrderRepository;
import com.wims.backend.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
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
public class VNPayService {

    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    public String createPaymentUrl(long orderId, String ipAddress) {

        // 1. Lấy user hiện tại để bảo mật (chỉ chủ đơn hàng mới được tạo link thanh toán lại)
        User user = securityUtils.getCurrentUserLogin();
        String username = user.getUsername();

        // 2. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(1004, "Đơn hàng không tồn tại"));

        // 3. Check quyền sở hữu
        if (!order.getUser().getUsername().equals(username)) {
            throw new AppException(403, "Bạn không có quyền thanh toán đơn hàng này");
        }

        // 4. Check trạng thái (Chỉ cho phép thanh toán nếu đang chờ)
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new AppException(1009, "Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // --- BẮT ĐẦU TẠO URL VNPAY ---
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(order.getId());
        // Lưu ý: VNPay tính tiền theo đơn vị đồng * 100 (VD: 10000 vnđ -> 1000000)
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
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build URL
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    public ApiResponse<VNPayResponse> handleCallback(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");

        // Lấy toàn bộ tham số
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = (String) params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");

        // Loại bỏ 2 trường hash để tính toán lại
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        // Tính toán lại chữ ký
        String signValue = VNPayConfig.hashAllFields(fields);

        if (signValue.equals(vnp_SecureHash)) {
            // Chữ ký hợp lệ -> Tiếp tục xử lý
            if ("00".equals(status)) {
                // Lấy các tham số
                String orderIdStr = request.getParameter("vnp_TxnRef");
                String transactionId = request.getParameter("vnp_TransactionNo");
                String paymentTime = request.getParameter("vnp_PayDate");
                String totalPrice = request.getParameter("vnp_Amount"); // Đơn vị: VNĐ * 100

                // --- 2. UPDATE DATABASE ---
                try {
                    Long orderId = Long.parseLong(orderIdStr);
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

                    // Kiểm tra số tiền (Quan trọng: Số tiền VNPay trả về nhân 100)
                    // Ví dụ: DB lưu 100,000 -> VNPay trả về 10,000,000
                    BigDecimal vnpAmount = new BigDecimal(totalPrice).divide(new BigDecimal(100));

                    if (order.getTotalAmount().compareTo(vnpAmount) == 0) {
                        // Update trạng thái
                        // Bạn có thể dùng enum PAID hoặc CONFIRMED tùy logic
                        order.setStatus(OrderStatus.PAID);
                        order.setPaymentTime(LocalDateTime.now());
                        orderRepository.save(order);

                        return ApiResponse.<VNPayResponse>builder()
                                .code(1000)
                                .message("Thanh toán thành công")
                                .result(VNPayResponse.builder()
                                        .transactionId(transactionId)
                                        .orderId(orderIdStr)
                                        .paymentTime(paymentTime)
                                        .totalPrice(totalPrice)
                                        .build())
                                .build();
                    } else {
                        return ApiResponse.<VNPayResponse>builder()
                                .code(7777)
                                .message("Số tiền thanh toán không khớp")
                                .build();
                    }
                } catch (Exception e) {
                    return ApiResponse.<VNPayResponse>builder()
                            .code(7777)
                            .message("Lỗi xử lý đơn hàng: " + e.getMessage())
                            .build();
                }
            } else {
                return ApiResponse.<VNPayResponse>builder()
                        .code(7777)
                        .message("Giao dịch thất bại tại VNPay")
                        .build();
            }
        } else {
            return ApiResponse.<VNPayResponse>builder()
                    .code(7777)
                    .message("Chữ ký không hợp lệ (Checksum failed)")
                    .build();
        }
    }
}
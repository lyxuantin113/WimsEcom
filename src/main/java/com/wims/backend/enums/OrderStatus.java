package com.wims.backend.enums;

public enum OrderStatus {
    // --- Giai đoạn khởi tạo ---
    PENDING_PAYMENT, // (Mới) Áp dụng cho VNPay: Đã tạo đơn, đang chờ khách trả tiền.
    PENDING_CONFIRMATION, // (Cũ là PENDING) Áp dụng cho COD: Chờ Admin/CSKH duyệt.

    // --- Giai đoạn xử lý ---
    PAID,       // (Mới) Khách đã trả tiền qua VNPay thành công (Tương đương Confirmed).
    CONFIRMED,  // Admin đã xác nhận đơn COD (hoặc chuyển từ PAID sang đây để in đơn).

    // --- Giai đoạn giao vận ---
    SHIPPING,   // Đang giao hàng
    COMPLETED,  // Giao thành công (Tiền đã về túi)

    // --- Giai đoạn Hủy/Trả ---
    CANCELLED,  // Hủy đơn
    RETURN_REQUESTED,
    RETURNED
}

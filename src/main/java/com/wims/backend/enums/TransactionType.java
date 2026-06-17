package com.wims.backend.enums;

public enum TransactionType {
    IMPORT,    // Nhập hàng
    EXPORT,    // Bán ra
    RETURN,    // Hoàn trả (Từ đơn hàng bị hủy/trả lại)
    ADJUSTMENT // Điều chỉnh kho (Khi kiểm kê thấy chênh lệch)
}

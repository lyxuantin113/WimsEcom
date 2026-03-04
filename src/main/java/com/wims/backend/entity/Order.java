package com.wims.backend.entity;

import com.wims.backend.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_customer_name", columnList = "customerName"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_user_id", columnList = "user_id"),
        @Index(name = "idx_order_created_at", columnList = "createdAt")
}) // BẮT BUỘC: Tránh trùng từ khóa Order của SQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Khi gọi repo.delete(id), nó chạy câu lệnh SQL này:
@SQLDelete(sql = "UPDATE users SET is_deleted = true, email = CONCAT(email, '_deleted_', CURRENT_TIMESTAMP), username = CONCAT(username, '_deleted_', CURRENT_TIMESTAMP) WHERE id = ?")
// Khi gọi repo.findAll(), nó tự động nối thêm điều kiện này:
@Where(clause = "is_deleted = false")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ai mua đơn này?
    // Để đơn giản giai đoạn này, ta lưu userId thôi cũng được,
    // hoặc join ManyToOne với User nếu muốn chặt chẽ. Ta cứ join cho xịn nhé.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // CUSTOMER INFO
    private String customerName; // Tên người nhận (có thể khác tên user)
    private String phone; // SĐT người nhận
    private String address; // Địa chỉ giao hàng

    private BigDecimal totalAmount; // Tổng tiền hóa đơn

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // Trạng thái (PENDING,...)

    // PAYMENT
    private String paymentMethod; // "COD" hoặc "VNPAY"
    private LocalDateTime paymentTime; // Thời gian thanh toán thành công

    // DISCOUNT
    private String discountCode;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private LocalDateTime createdAt; // Ngày đặt hàng

    // Soft Delete
    @Column(name = "is_deleted")
    private boolean deleted = false;

    // Quan hệ 1 Order - N OrderDetail
    // mappedBy = "order": Tên biến "order" nằm bên OrderDetail
    // cascade = ALL: Xóa Order thì xóa luôn các món bên trong
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
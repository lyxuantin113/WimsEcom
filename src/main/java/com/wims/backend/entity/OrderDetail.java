package com.wims.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thuộc về đơn hàng nào?
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order; // Biến này tên là "order", khớp với mappedBy ở trên

    // Mua sản phẩm nào?
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity; // Số lượng mua

    private BigDecimal price; // GIÁ TẠI THỜI ĐIỂM MUA (Quan trọng!)

    @Column(name = "is_discounted")
    private boolean isDiscounted = false;

    // Có thể thêm hàm tiện ích tính thành tiền của dòng này
    // public BigDecimal getSubTotal() {
    //     return price.multiply(BigDecimal.valueOf(quantity));
    // }
}
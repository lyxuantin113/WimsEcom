package com.wims.backend.entity;

import com.wims.backend.enums.DiscountScope;
import com.wims.backend.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Mã voucher (VD: HE2025)

    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType type;

    @Column(name = "discount_value")
    private BigDecimal value; // Giá trị (VD: 10 hoặc 50000)

    @Enumerated(EnumType.STRING)
    private DiscountScope scope;

    // Lưu danh sách ID được áp dụng dạng chuỗi "1,2,3"
    // Nếu GLOBAL thì để null
    private String applicableIds;

    private Integer usageLimit; // Giới hạn số lượng (VD: 100 mã)
    private Integer usedCount; // Đã dùng (VD: 5 mã)

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private BigDecimal minOrderValue; // Đơn tối thiểu để áp dụng
    private BigDecimal maxDiscountAmount; // Giảm tối đa (cho loại %) - VD: Giảm 10% nhưng tối đa 50k

    private boolean active = true;
}

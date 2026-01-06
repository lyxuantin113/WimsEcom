package com.wims.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity // 1. Báo cho Spring biết: Class này đại diện cho 1 bảng trong DB
@Table(name = "products") // 2. Tên bảng trong DB sẽ là "products"
@Data // 3. Lombok tự sinh Getter, Setter, toString, hashCode... (Khỏi phải viết tay)
// 1. Khi gọi hàm delete(), thay vì DELETE thật, hãy chạy câu lệnh UPDATE này
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id = ? AND version = ?")
// 2. Khi gọi hàm select (findAll, findById...), tự động thêm điều kiện này vào
@Where(clause = "is_deleted = false")
public class Product extends BaseEntity {

    @Id // Đây là Khóa chính (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng (Auto Increment)
    private Long id;

    @Column(nullable = false, unique = true) // Không được null, không được trùng tên
    private String code; // Mã sản phẩm (SKU)

    @Column(nullable = false)
    private String name; // Tên sản phẩm

    private String description; // Mô tả (có thể null)

    @Column(precision = 19, scale = 2) // Định dạng số tiền (19 số, 2 số thập phân)
    private BigDecimal price; // Giá bán

    private Integer stockQuantity; // Số lượng tồn kho

    @Column(name = "image")
    private String image;

    @Version
    private Long version;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(updatable = false) // Không cho phép sửa ngày tạo
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Relations

    // @ManyToOne: Nhiều sản phẩm thuộc về 1 danh mục
    // @JoinColumn: Tên cột khóa ngoại trong Database sẽ là "category_id"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}
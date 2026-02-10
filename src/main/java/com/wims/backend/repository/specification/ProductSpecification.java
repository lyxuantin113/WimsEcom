package com.wims.backend.repository.specification;

import com.wims.backend.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public class ProductSpecification {

    // 1. Lọc theo tên (Chứa từ khóa, không phân biệt hoa thường)
    public static Specification<Product> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return null; // Nếu tên rỗng -> Không lọc gì cả
            }

            // Logic: WHERE LOWER(name) LIKE %name%
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    // 2. Lọc theo khoảng giá (minPrice <= price <= maxPrice)
    public static Specification<Product> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return null; // Không truyền giá -> Không lọc
            }

            if (minPrice != null && maxPrice != null) {
                // WHERE price BETWEEN min AND max
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            }

            if (minPrice != null) {
                // WHERE price >= min
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            }

            // Trường hợp còn lại: WHERE price <= max
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    // 3. Hết hàng
    public static Specification<Product> isOutOfStock(boolean isOutOfStock) {
        return (root, query, criteriaBuilder) -> {
            if (!isOutOfStock) return null;

            return criteriaBuilder.lessThanOrEqualTo(root.get("stockQuantity"), 0);
        };
    }

    // 4. Category
    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) return null;

            // Truy cập vào bảng quan hệ 'category', lấy trường 'id' so sánh
            // Với "category" là tên biến @ManyToOne trong Entity Product
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }
}
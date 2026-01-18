package com.wims.backend.repository;

import com.wims.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Đánh dấu đây là Bean giao tiếp DB
public interface ProductRepository extends
        JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {
    // JpaRepository đã có sẵn các hàm: save(), findAll(), findById(), delete()...
    // Chúng ta chỉ cần khai báo thêm các hàm tìm kiếm đặc thù nếu cần.

    // Ví dụ: Tìm sản phẩm theo Mã code (để check trùng)
    // Spring sẽ tự dịch cái tên hàm này thành SQL: SELECT * FROM products WHERE code = ?
    Optional<Product> findByCode(String code);

    Product getProductByIdIs(Long id);

    // Related Products
    Page<Product> findByCategoryIdAndIdNot(Long categoryId, Long excludedId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE YEAR(p.createdAt) = :year")
    long countProductByYear(@Param("year") int year);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    void incrementStock(@Param("id") Long id, @Param("quantity") int quantity);
}
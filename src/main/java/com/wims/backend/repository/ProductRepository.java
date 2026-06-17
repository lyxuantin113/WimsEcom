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

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

@Repository // Đánh dấu đây là Bean giao tiếp DB
public interface ProductRepository extends
        JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // SELECT * FROM products WHERE code = ?
    Optional<Product> findByCode(String code);

    // Related Products
    Page<Product> findByCategoryIdAndIdNot(Long categoryId, Long excludedId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") })
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdWithLock(@Param("ids") Iterable<Long> ids);

    @Query("SELECT COUNT(p) FROM Product p WHERE YEAR(p.createdAt) = :year")
    long countProductByYear(@Param("year") int year);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    void incrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    List<Product> findByStockQuantityLessThan(Integer threshold);
}
package com.wims.backend.repository;

import com.wims.backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng theo User ID
    Cart findByUserId(Long userId);

    // Hoặc kiểm tra xem user đã có giỏ hàng chưa
    boolean existsByUserId(Long userId);
}

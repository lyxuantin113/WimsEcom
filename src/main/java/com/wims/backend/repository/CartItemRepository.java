package com.wims.backend.repository;

import com.wims.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm xem trong giỏ hàng này đã có sản phẩm này chưa (để cộng dồn số lượng)
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}

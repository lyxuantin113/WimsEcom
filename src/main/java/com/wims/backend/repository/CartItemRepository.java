package com.wims.backend.repository;

import com.wims.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm xem trong giỏ hàng này đã có sản phẩm này chưa (để cộng dồn số lượng)
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Modifying // Bắt buộc vì đây là lệnh thay đổi dữ liệu (UPDATE/DELETE)
    // Query xóa thẳng trong DB, không lôi lên RAM
    @Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);
}

package com.wims.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Một User có 1 giỏ hàng
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Một giỏ hàng có nhiều món (Cascade ALL để xóa giỏ là xóa luôn món bên trong)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    // Helper remmove
    public void removeCartItem(CartItem item) {
        this.cartItems.remove(item); // Xóa khỏi list của cha
        item.setCart(null);          // Cắt đứt quan hệ từ phía con
    }

    // Helper method để thêm item cho dễ (đồng bộ 2 chiều)
    public void addItem(CartItem item) {
        this.cartItems.add(item);
        item.setCart(this);
    }
}

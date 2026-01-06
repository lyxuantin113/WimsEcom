package com.wims.backend.service;

import com.wims.backend.dto.response.CartResponse;
import com.wims.backend.entity.Cart;
import com.wims.backend.entity.CartItem;
import com.wims.backend.entity.Product;
import com.wims.backend.entity.User;
import com.wims.backend.repository.CartItemRepository;
import com.wims.backend.repository.CartRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    // 1. Khai báo các Mock (Bỏ CartMapper đi vì code bạn không dùng)
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    // Hàm giả lập đăng nhập
    private void mockLoginUser(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("getMyCart: Phải tự động xóa Item rác (Product=null) và tính tổng tiền đúng")
    void getMyCart_ShouldCleanNullProducts_AndCalculateTotal() {
        // --- GIVEN (Chuẩn bị dữ liệu) ---
        String username = "testUser";
        mockLoginUser(username);

        User user = new User();
        user.setId(1L);

        // Item 1: Hợp lệ (Giá 100, Số lượng 2 -> Tổng 200)
        Product validProduct = new Product();
        validProduct.setId(101L);
        validProduct.setPrice(BigDecimal.valueOf(100));
        validProduct.setImage("img.jpg");
        validProduct.setName("Product A");

        CartItem validItem = new CartItem();
        validItem.setId(1L);
        validItem.setProduct(validProduct);
        validItem.setQuantity(2);

        // Item 2: RÁC (Product = null do bị Soft Delete nên Hibernate không load lên)
        CartItem invalidItem = new CartItem();
        invalidItem.setId(2L);
        invalidItem.setProduct(null); // <--- Mấu chốt là đây
        invalidItem.setQuantity(5);

        // List Cart Items (Cần dùng ArrayList để hỗ trợ iterator.remove())
        List<CartItem> items = new ArrayList<>();
        items.add(validItem);
        items.add(invalidItem);

        Cart cart = new Cart();
        cart.setId(99L);
        cart.setUser(user);
        cart.setCartItems(items);
        // Gán ngược lại cart cho item để đúng logic entity
        validItem.setCart(cart);
        invalidItem.setCart(cart);

        // Mock hành vi Repository
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(cart);

        // --- WHEN (Chạy hàm) ---
        CartResponse response = cartService.getMyCart();

        // --- THEN (Kiểm tra kết quả) ---

        // 1. Kiểm tra xem hàm delete có được gọi cho Item rác không?
        verify(cartItemRepository, times(1)).delete(invalidItem);

        // 2. Kiểm tra Response trả về
        Assertions.assertNotNull(response);

        // List trả về chỉ được có 1 item (Item rác đã bị lọc)
        Assertions.assertEquals(1, response.getItems().size());
        Assertions.assertEquals(101L, response.getItems().get(0).getProductId());

        // 3. Kiểm tra TỔNG TIỀN (Quan trọng)
        // Item rác (null product) không được tính vào tổng tiền
        // Tổng tiền đúng = 100 * 2 = 200
        BigDecimal expectedTotal = BigDecimal.valueOf(200);
        Assertions.assertEquals(0, expectedTotal.compareTo(response.getTotalAmount()));
    }
}
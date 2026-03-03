package com.wims.backend.service.based;

import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.response.CartItemResponse;
import com.wims.backend.dto.response.CartResponse;
import com.wims.backend.entity.Cart;
import com.wims.backend.entity.CartItem;
import com.wims.backend.entity.Product;
import com.wims.backend.entity.User;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.CartItemRepository;
import com.wims.backend.repository.CartRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private final SecurityUtils securityUtils;

    // Hàm nội bộ để lấy hoặc tạo giỏ hàng
    private Cart getOrCreateCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCartItems(new ArrayList<>());
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    @Transactional
    public CartResponse addToCart(CartItemRequest request) {

        // Lấy Entity User thật
        User user = securityUtils.getCurrentUserLogin();

        // 2. Tìm Product
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new AppException(1004, "Sản phẩm không tồn tại"));

        // 3. Lấy Cart
        Cart cart = getOrCreateCart(user);

        // A. Kiểm tra số lượng âm (Validation)
        if (request.quantity() <= 0) {
            throw new AppException(1015, "Số lượng sản phẩm phải lớn hơn 0");
        }

        // B. Tính toán tổng số lượng dự kiến (Trong giỏ + thêm)
        int currentQuantityInCart = 0;

        // Tìm xem sản phẩm đã có trong giỏ chưa để lấy số lượng cũ
        Optional<CartItem> existingItemCheck = cartItemRepository.findByCartIdAndProductId(cart.getId(),
                product.getId());

        if (existingItemCheck.isPresent()) {
            currentQuantityInCart = existingItemCheck.get().getQuantity();
        }

        int totalRequestedQuantity = currentQuantityInCart + request.quantity();

        // C. Kiểm tra tồn kho
        if (totalRequestedQuantity > product.getStockQuantity()) {
            throw new AppException(1016,
                    "Số lượng vượt quá tồn kho. Kho còn: " + product.getStockQuantity() +
                            ", Trong giỏ bạn đang có: " + currentQuantityInCart);
        }

        // 4. Logic thêm/sửa item
        if (existingItemCheck.isPresent()) {
            // Case 1: Đã có -> Cộng dồn
            CartItem existingItem = existingItemCheck.get();
            existingItem.setQuantity(totalRequestedQuantity);
        } else {
            // Case 2: Chưa có -> Tạo mới
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .cart(cart)
                    .quantity(request.quantity())
                    .build();
            cart.getCartItems().add(newItem);
        }

        // 5. Dirty Checking
        // cart = cartRepository.save(cart);

        // 6. Trả về response
        return toCartResponse(cart);
    }

    // Lấy giỏ hàng hiện tại
    public CartResponse getMyCart() {
        User currentUser = securityUtils.getCurrentUserLogin();
        Cart cart = getOrCreateCart(currentUser); // Tái sử dụng hàm này để đảm bảo luôn có giỏ trả về
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(Long itemId, int quantity) {
        // 1. Tìm CartItem
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(1014, "Sản phẩm trong giỏ không tồn tại"));

        // 2. Validate số lượng
        if (quantity <= 0) {
            // Nếu số lượng <= 0 thì xóa luôn
            cartItemRepository.delete(item);
        } else {
            // Check tồn kho
            if (quantity > item.getProduct().getStockQuantity()) {
                throw new AppException(1016, "Kho chỉ còn " + item.getProduct().getStockQuantity());
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        // 3. Trả về giỏ hàng mới nhất (để FE update lại giao diện)
        // item.getCart() có thể chưa update list items trong bộ nhớ,
        // nên an toàn nhất là query lại cart của user đó.
        return getMyCart();
    }

    @Transactional
    public CartResponse removeCartItem(Long itemId) {
        // 1. Tìm item
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(1004, "Sản phẩm trong giỏ không tồn tại"));

        // 2. Lấy Cha (Cart)
        Cart cart = item.getCart();

        // 3. Xóa item khỏi List của Cha (Lúc này Memory đã sạch)
        cart.removeCartItem(item);

        // 4. Lưu lại Cha -> Nhờ orphanRemoval=true, Hibernate sẽ tự bắn lệnh DELETE
        // xuống DB
        cartRepository.save(cart);

        // 5. Trả về Cart (Lúc này Cart trong memory đã mất item đó rồi, nên Response sẽ
        // đúng)
        return toCartResponse(cart);
    }

    // KHÔNG RECOMMEND - NÊN XÓA RIÊNG Ở HÀM KHÁC MAPPER
    // Mapper để xóa các Item (Product) đang trong giỏ mà Item (Product) đó bị Soft
    // Delete
    // Tránh lôix 400 - gọi tính toán dù đang null
    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Duyệt qua từng item trong giỏ (dùng iterator để có thể remove an toàn)
        Iterator<CartItem> iterator = cart.getCartItems().iterator();

        while (iterator.hasNext()) {
            CartItem item = iterator.next();

            // FIX LỖI: Kiểm tra nếu Product bị null (do bị xóa mềm)
            if (item.getProduct() == null) {
                // Cách 1: Tự động dọn dẹp (Xóa item rác này khỏi DB luôn)
                cartItemRepository.delete(item);
                iterator.remove(); // Xóa khỏi list hiện tại để tính toán không sai
                continue; // Bỏ qua vòng lặp này
            }

            // Logic cũ của bạn (Giờ đã an toàn)
            BigDecimal itemTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            totalAmount = totalAmount.add(itemTotal);

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .productImage(item.getProduct().getImage())
                    .price(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .totalPrice(itemTotal)
                    .build());
        }

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .build();
    }
}

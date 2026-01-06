package com.wims.backend.service;

import com.wims.backend.dto.request.OrderCreationRequest;
import com.wims.backend.dto.response.OrderResponse;
import com.wims.backend.entity.Order;
import com.wims.backend.entity.User;
import com.wims.backend.event.OrderCreatedEvent;
import com.wims.backend.mapper.OrderMapper;
import com.wims.backend.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private DiscountService discountService;
    @Mock private OrderMapper orderMapper;

    // 🔥 MOCK QUAN TRỌNG NHẤT: Cái loa phát thanh
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private void mockLoginUser(String username) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("createOrder: Phải bắn ra sự kiện OrderCreatedEvent sau khi lưu đơn")
    void createOrder_ShouldPublishEvent() {
        // --- GIVEN ---
        String username = "testUser";
        mockLoginUser(username);

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        // Request tạo đơn
        OrderCreationRequest request = new OrderCreationRequest();
        request.setCustomerName("Mr A");
        request.setPhone("0999");
        request.setAddress("HCM");
        request.setPaymentMethod("COD");
        request.setItems(new ArrayList<>()); // Item rỗng cho nhanh (ta ko test logic trừ kho ở đây)

        // Mock User
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Mock save order
        Order savedOrder = new Order();
        savedOrder.setId(999L);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Mock Mapper
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(new OrderResponse());

        // --- WHEN ---
        orderService.createOrder(request);

        // --- THEN ---

        // VERIFY QUAN TRỌNG: Kiểm tra xem Event có được publish không?
        // Nếu dòng này fail -> Code tách async chưa chạy.
        verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));

        // Kiểm tra xem có save order không
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
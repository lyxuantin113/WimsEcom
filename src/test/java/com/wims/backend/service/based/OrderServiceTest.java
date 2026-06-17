package com.wims.backend.service.based;

import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.request.DiscountCalculationRequest;
import com.wims.backend.dto.request.OrderCreationRequest;
import com.wims.backend.dto.response.DiscountCalculationResponse;
import com.wims.backend.dto.response.OrderResponse;
import com.wims.backend.entity.*;
import com.wims.backend.event.OrderCreatedEvent;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.exception.AppException;
import com.wims.backend.mapper.OrderMapper;
import com.wims.backend.repository.*;
import com.wims.backend.service.payment.PaymentFactory;
import com.wims.backend.service.payment.PaymentStrategy;
import com.wims.backend.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private PaymentFactory paymentFactory;
    @Mock
    private PaymentStrategy paymentStrategy;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private DiscountRepository discountRepository;
    @Mock
    private DiscountService discountService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SecurityUtils securityUtils;

    private OrderService orderService;

    private User mockUser;
    private Product mockProduct;
    private OrderCreationRequest mockRequest;
    private CartItemRequest mockItemRequest;
    private Discount mockDiscount;
    private Order mockOrderSaved;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, productRepository, cartRepository, paymentFactory, cartItemRepository,
                inventoryService, orderMapper, discountRepository, discountService, eventPublisher, securityUtils);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("MacBook");
        mockProduct.setPrice(BigDecimal.valueOf(2000));
        mockProduct.setStockQuantity(10); // Đủ hàng

        mockItemRequest = new CartItemRequest(100L, 2);

        mockRequest = new OrderCreationRequest(
                "Xuan Tin",
                "0123456789",
                "HCM",
                "COD",
                List.of(mockItemRequest),
                null);

        mockDiscount = new Discount();
        mockDiscount.setCode("TET2026");
        mockDiscount.setUsedCount(0);

        mockOrderSaved = new Order();
        mockOrderSaved.setId(999L);
        mockOrderSaved.setTotalAmount(BigDecimal.valueOf(4000));
    }

    @Test
    @DisplayName("Tạo đơn hàng thành công (Không áp mã giảm giá)")
    void createOrder_Success_NoDiscount() {
        // Arrange
        when(securityUtils.getCurrentUserLogin()).thenReturn(mockUser);
        when(productRepository.findAllByIdWithLock(anyList())).thenReturn(List.of(mockProduct));

        when(paymentFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.getInitialOrderStatus()).thenReturn(OrderStatus.PENDING_CONFIRMATION);

        when(orderRepository.save(any(Order.class))).thenReturn(mockOrderSaved);

        OrderResponse responseMock = OrderResponse.builder()
                .id(999L)
                .totalAmount(BigDecimal.valueOf(4000))
                .build();
        when(orderMapper.toOrderResponse(mockOrderSaved)).thenReturn(responseMock);

        // Giả lập giỏ hàng để xóa
        Cart mockCart = new Cart();
        mockCart.setId(888L);
        when(cartRepository.findByUserId(1L)).thenReturn(mockCart);

        // Act
        OrderResponse result = orderService.createOrder(mockRequest);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.id());
        assertEquals(BigDecimal.valueOf(4000), result.totalAmount());

        // Kiểm tra trừ kho
        verify(inventoryService, times(1)).exportStock(eq(100L), eq(2), eq(999L), anyString());

        // Kiểm tra xóa giỏ hàng
        verify(cartItemRepository, times(1)).deleteAllByCartId(888L);

        // Kiểm tra có bắn event
        verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("Tạo đơn hàng thất bại - Hết hàng (Ném lỗi 1005)")
    void createOrder_Fail_OutOfStock() {
        // Arrange
        mockProduct.setStockQuantity(1); // Chỉ còn 1, nhưng yêu cầu mua 2
        when(securityUtils.getCurrentUserLogin()).thenReturn(mockUser);
        when(productRepository.findAllByIdWithLock(anyList())).thenReturn(List.of(mockProduct));

        when(paymentFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.getInitialOrderStatus()).thenReturn(OrderStatus.PENDING_CONFIRMATION);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            orderService.createOrder(mockRequest);
        });

        assertEquals(1005, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("hết hàng"));

        // Đảm bảo không lưu Order nào
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Tạo đơn hàng thành công CÓ áp mã giảm giá")
    void createOrder_Success_WithDiscount() {
        // Arrange
        mockRequest = new OrderCreationRequest(
                "Xuan Tin",
                "0123456789",
                "HCM",
                "COD",
                List.of(mockItemRequest),
                "TET2026");
        when(securityUtils.getCurrentUserLogin()).thenReturn(mockUser);
        when(productRepository.findAllByIdWithLock(anyList())).thenReturn(List.of(mockProduct));
        when(discountRepository.findByCodeWithLock("TET2026")).thenReturn(Optional.of(mockDiscount));

        when(paymentFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.getInitialOrderStatus()).thenReturn(OrderStatus.PENDING_CONFIRMATION);

        // Giả lập discount service trả về kết quả giảm 500
        DiscountCalculationResponse calcResponse = DiscountCalculationResponse.builder()
                .totalDiscount(BigDecimal.valueOf(500))
                .affectedProductIds(List.of(100L))
                .build();
        when(discountService.calculateDiscount(any(DiscountCalculationRequest.class))).thenReturn(calcResponse);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
        });

        OrderResponse responseMock = OrderResponse.builder()
                .id(999L)
                .totalAmount(BigDecimal.valueOf(3500))
                .discountAmount(BigDecimal.valueOf(500))
                .build();
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(responseMock);

        // Act
        OrderResponse result = orderService.createOrder(mockRequest);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(3500), result.totalAmount());
        assertEquals(BigDecimal.valueOf(500), result.discountAmount());

        // Đảm bảo voucher đã được tăng useCount
        verify(discountRepository, times(1)).save(mockDiscount);
        assertEquals(1, mockDiscount.getUsedCount());
    }
}

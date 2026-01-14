package com.wims.backend.service.based;

import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.request.DiscountCalculationRequest;
import com.wims.backend.dto.request.OrderCreationRequest;
import com.wims.backend.dto.response.DiscountCalculationResponse;
import com.wims.backend.dto.response.OrderResponse;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.entity.*;
import com.wims.backend.enums.OrderStatus;
import com.wims.backend.event.OrderCreatedEvent;
import com.wims.backend.event.OrderStatusChangedEvent;
import com.wims.backend.exception.AppException;
import com.wims.backend.mapper.OrderMapper;
import com.wims.backend.repository.*;
import com.wims.backend.security.CustomUserDetails;
import com.wims.backend.service.feature.NotificationService;
import com.wims.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository; // Để lấy thông tin người mua
    private final CartRepository cartRepository;

    private final OrderMapper orderMapper;

    private final DiscountRepository discountRepository;
    private final DiscountService discountService;

    private final ApplicationEventPublisher eventPublisher;

    private final SecurityUtils securityUtils;

    public PageResponse<OrderResponse> getAllOrders(int page, int size, String sortBy) {
        Sort sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Order> orderPage = orderRepository.findAll(pageable);

        Page<OrderResponse> orderResponsePage = orderPage.map(orderMapper::toOrderResponse);

        return PageResponse.<OrderResponse>builder()
                .currentPage(page)
                .totalPages(orderResponsePage.getTotalPages())
                .pageSize(orderResponsePage.getSize())
                .totalElements(orderResponsePage.getTotalElements())
                .data(orderResponsePage.getContent())
                .build();
    }

    public PageResponse<OrderResponse> getMyOrders(
            int page,
            int size,
            String sortBy
    ) {

        Sort sort = Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        User user = securityUtils.getCurrentUserLogin();

        if (user == null) {
            throw new AppException(1001, "User không tồn tại !");
        }

        Page<Order> orderList = orderRepository.findByUserId(user.getId(), pageable);

        Page<OrderResponse> orderListResponse = orderList.map(orderMapper::toOrderResponse);

        return PageResponse.<OrderResponse>builder()
                .currentPage(page)
                .totalPages(orderListResponse.getTotalPages())
                .pageSize(orderListResponse.getSize())
                .totalElements(orderList.getTotalElements())
                .data(orderListResponse.getContent())
                .build();
    }

    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request) {

        // 1. Lấy User
        User user = securityUtils.getCurrentUserLogin();

        // 2. Xác định trạng thái ban đầu
        OrderStatus initialStatus = "VNPAY".equalsIgnoreCase(request.getPaymentMethod())
                ? OrderStatus.PENDING_PAYMENT
                : OrderStatus.PENDING_CONFIRMATION;

        // 3. Khởi tạo Order (Chưa save vội)
        // & Query -> Map tránh N+1
        Order order = Order.builder()
                .user(user)
                .customerName(request.getCustomerName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                // Các trường tiền sẽ set sau
                .build();

        List<Long> productIds = request.getItems().stream()
                .map(CartItemRequest::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // Validate: Check xem có sản phẩm nào không tìm thấy không
        if (products.size() != productIds.size()) {
            throw new AppException(1003, "Một số sản phẩm không tồn tại hoặc đã bị xóa");
        }

        // 4. Xử lý Items & Kho & Tính Raw Total
        List<OrderDetail> details = new ArrayList<>();
        BigDecimal rawTotal = BigDecimal.ZERO; // Tổng tiền hàng chưa giảm giá

        // 5. XỬ LÝ DISCOUNT (Tính trước khi Save)
        BigDecimal discountAmount = BigDecimal.ZERO;
        List<Long> affectedProductIds = new ArrayList<>();

        if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
            Discount discount = discountRepository.findByCodeAndActiveTrue(request.getDiscountCode())
                    .orElseThrow(() -> new AppException(1001, "Mã giảm giá không hợp lệ"));

            // Gọi service tính toán
            DiscountCalculationRequest discountReq = new DiscountCalculationRequest();
            discountReq.setCode(request.getDiscountCode());
            discountReq.setItems(request.getItems());

            DiscountCalculationResponse discountCalculationRes = discountService.calculateDiscount(discountReq);
            discountAmount = discountCalculationRes.getTotalDiscount();
            affectedProductIds = discountCalculationRes.getAffectedProductIds();

            // Trừ lượt sử dụng (Nên dùng query update atomic để tránh race condition nếu lượt truy cập cao)
            discount.setUsedCount(discount.getUsedCount() + 1);
            discountRepository.save(discount);

            // Set thông tin voucher vào order
            order.setDiscountCode(request.getDiscountCode());
            order.setDiscountAmount(discountAmount);
        }

        for (CartItemRequest item : request.getItems()) {
            Product product = productMap.get(item.getProductId());

            // Check & Trừ kho
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new AppException(1005, "Sản phẩm " + product.getName() + " hết hàng");
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());

            // Tạo Detail
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .isDiscounted(affectedProductIds.contains(product.getId()))
                    .build();
            details.add(detail);

            // Cộng dồn
            rawTotal = rawTotal.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        productRepository.saveAll(products);

        // Gán list detail vào order
        order.setOrderDetails(details);

        BigDecimal finalTotal = rawTotal;

        // 6. Chốt giá cuối cùng
        finalTotal = rawTotal.subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        order.setTotalAmount(finalTotal);

        // 7. LƯU ORDER (Chỉ gọi save 1 lần duy nhất ở đây)
        Order savedOrder = orderRepository.save(order);

        // 8. Xóa giỏ hàng
        Cart cart = cartRepository.findByUserId(user.getId());
        if (cart != null) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }

        // 9. Fix lỗi Email trong Transaction: BẮN EVENT
        // Transaction chưa commit tại đây, nhưng Event Listener sẽ xử lý sau
        eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder, user));

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String statusString) {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(1004, "Đơn hàng không tồn tại"));

        OrderStatus oldStatus = order.getStatus();
        // 2. Validate trạng thái gửi lên (Tránh gửi linh tinh "ABCXYZ")
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(1005, "Trạng thái không hợp lệ: " + statusString);
        }

        // 3. Logic HOÀN KHO (Chỉ chạy khi đơn bị HỦY)
        restock(order, newStatus);

        // 4. Cập nhật trạng thái mới
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, savedOrder, oldStatus, newStatus));

        // 5. Lưu và trả về
        return orderMapper.toOrderResponse(savedOrder);
    }

    // Lấy chi tiết đơn hàng (Có bảo mật: Chỉ Admin hoặc Chính chủ mới được xem)
    public OrderResponse getOrderById(Long orderId) {
        // Tìm đơn hàng trong DB
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(1004, "Đơn hàng không tồn tại"));

        // Check xem người này có quyền ADMIN không?
        User currentUser = securityUtils.getCurrentUserLogin();

        // 3. LOGIC BẢO VỆ: So sánh ID thay vì Username
        // Logic: Nếu KHÔNG PHẢI Admin VÀ ID người dùng KHÔNG TRÙNG ID chủ đơn -> Chặn
        if (!securityUtils.isAdmin() && !currentUser.getId().equals(order.getUser().getId())) {
            throw new AppException(403, "Bạn không có quyền xem đơn hàng của người khác!");
        }

        // 5. Nếu qua được cửa ải trên -> Trả về dữ liệu
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        User user = securityUtils.getCurrentUserLogin();
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(1001, "Đơn hàng không tồn tại"));

        if (!order.getUser().getUsername().equals(user.getUsername())) {
            throw new AppException(403, "Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION
                && order.getStatus() != OrderStatus.CONFIRMED
                && order.getStatus() != OrderStatus.PENDING_PAYMENT) {

            throw new AppException(1009, "Đơn hàng đang giao hoặc đã hoàn thành, không thể hủy!");
        }
        else if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(1009, "Đơn hàng đã bị hủy trước đó");
        }

        restock(order, OrderStatus.CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse requestReturn(Long orderId) {
        User user = securityUtils.getCurrentUserLogin();
        String username = user.getUsername();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(1004, "Đơn hàng không tồn tại"));

        if (!order.getUser().getUsername().equals(username)) {
            throw new AppException(403, "Bạn không có quyền thao tác đơn hàng này");
        }

        // Chỉ được trả hàng khi đã Giao thành công
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new AppException(1009, "Chỉ đơn hàng đã hoàn thành mới được yêu cầu trả hàng");
        }

        order.setStatus(OrderStatus.RETURN_REQUESTED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    private void restock(Order order, OrderStatus newStatus) {
        if ((newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) ||
                (newStatus == OrderStatus.RETURNED && order.getStatus() != OrderStatus.RETURNED)) {

            List<Product> products = new ArrayList<>();

            for (OrderDetail detail : order.getOrderDetails()) {
                Product product = detail.getProduct();
                product.setStockQuantity(product.getStockQuantity() + detail.getQuantity());
                products.add(product);
            }

            productRepository.saveAll(products);

        }
    }
}

package com.wims.backend.service.based;

import com.wims.backend.configuration.DataSeeder;
import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.request.OrderCreationRequest;
import com.wims.backend.entity.Category;
import com.wims.backend.entity.Product;
import com.wims.backend.entity.Role;
import com.wims.backend.entity.User;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.CategoryRepository;
import com.wims.backend.repository.OrderRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.repository.RoleRepository;
import com.wims.backend.repository.UserRepository;
import com.wims.backend.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceConcurrencyTest {

    @MockBean
    private DataSeeder dataSeeder;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Xóa sạch DB trước mỗi test
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        categoryRepository.deleteAll();

        // 1. Tạo Category
        Category category = new Category();
        category.setName("Laptop");
        category = categoryRepository.save(category);

        // 2. Tạo Product với số lượng = 2
        Product product = new Product();
        product.setName("Concurrent MacBook");
        product.setCode("CMAC");
        product.setPrice(BigDecimal.valueOf(2000));
        product.setStockQuantity(2); // Chỉ có 2 sản phẩm trong kho
        product.setCategory(category);
        testProduct = productRepository.save(product);

        // 3. Tạo Role & User
        Role userRole = new Role();
        userRole.setName("USER");
        userRole = roleRepository.save(userRole);

        User user = new User();
        user.setUsername("concurrentuser");
        user.setPassword("password");
        user.setEmail("test@gmail.com");
        user.setFullName("Concurrent Test User");
        user.setRoles(Set.of(userRole));
        testUser = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Kiểm tra Pessimistic Lock - Trừ kho đồng thời không bị lỗi âm")
    void testConcurrentOrderCreation_StockDeduction() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 10 người cùng đặt 1 sản phẩm, mua số lượng = 1
        OrderCreationRequest request = new OrderCreationRequest();
        request.setCustomerName("Xuan Tin");
        request.setPhone("0123456789");
        request.setAddress("HCM");
        request.setPaymentMethod("COD");

        CartItemRequest item = new CartItemRequest();
        item.setProductId(testProduct.getId());
        item.setQuantity(1); // Mỗi người mua 1 cái
        request.setItems(List.of(item));

        // Act
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    // Set SecurityContext cho mỗi thread
                    CustomUserDetails userDetails = new CustomUserDetails(testUser);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // Execute order creation
                    orderService.createOrder(request);
                    successCount.incrementAndGet(); // Nếu ko văng lỗi tức là mua thành công
                } catch (AppException e) {
                    if (e.getErrorCode() == 1005) {
                        failCount.incrementAndGet(); // Lỗi hết hàng
                    } else {
                        System.err.println("Lỗi bất ngờ: " + e.getMessage());
                    }
                } finally {
                    SecurityContextHolder.clearContext();
                    latch.countDown();
                }
            });
        }

        // Đợi tất cả thread chạy xong
        latch.await();
        executorService.shutdown();

        // Assert
        System.out.println("Thành công: " + successCount.get());
        System.out.println("Thất bại (Hết hàng): " + failCount.get());

        // Vì kho chỉ có 2 cái, dù có 10 request đồng thời thì chỉ được phép có đúng 2
        // request thành công
        assertEquals(2, successCount.get());
        assertEquals(8, failCount.get());

        // Kiểm tra kho trên DB xem có bị âm không
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(0, updatedProduct.getStockQuantity());
    }
}

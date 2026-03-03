package com.wims.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wims.backend.configuration.TestSecurityConfig;
import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.request.OrderCreationRequest;
import com.wims.backend.dto.response.OrderResponse;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.security.JwtTokenProvider;
import com.wims.backend.security.CustomUserDetailsService;
import com.wims.backend.service.based.OrderService;
import com.wims.backend.service.infrastructure.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private OrderService orderService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private RedisService redisService;

        @MockBean
        private JpaMetamodelMappingContext jpaMappingContext;

        @MockBean(name = "auditorProvider")
        private AuditorAware<String> auditorProvider;

        @Autowired
        private ObjectMapper objectMapper;

        private OrderResponse mockOrderResponse;
        private OrderCreationRequest mockRequest;

        @BeforeEach
        void setUp() {
                mockOrderResponse = OrderResponse.builder()
                                .id(100L)
                                .customerName("Test Customer")
                                .totalAmount(BigDecimal.valueOf(5000))
                                .status("PENDING_CONFIRMATION")
                                .build();

                CartItemRequest item = new CartItemRequest(1L, 2);
                mockRequest = new OrderCreationRequest(
                                "Test Customer",
                                "0987654321",
                                "Hanoi",
                                "COD",
                                List.of(item),
                                null);
        }

        @Test
        @DisplayName("Tạo đơn hàng thành công")
        @WithMockUser(username = "testuser", roles = { "USER" })
        void createOrder_Success() throws Exception {
                Mockito.when(orderService.createOrder(any(OrderCreationRequest.class))).thenReturn(mockOrderResponse);

                String content = objectMapper.writeValueAsString(mockRequest);

                mockMvc.perform(post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.id").value(100))
                                .andExpect(jsonPath("$.result.customerName").value("Test Customer"));
        }

        @Test
        @DisplayName("Lấy danh sách đơn hàng của tôi thành công")
        @WithMockUser(username = "testuser", roles = { "USER" })
        void getMyOrders_Success() throws Exception {
                PageResponse<OrderResponse> pageResponse = PageResponse.<OrderResponse>builder()
                                .currentPage(1)
                                .pageSize(10)
                                .totalElements(1)
                                .totalPages(1)
                                .data(List.of(mockOrderResponse))
                                .build();

                Mockito.when(orderService.getMyOrders(anyInt(), anyInt(), anyString())).thenReturn(pageResponse);

                mockMvc.perform(get("/api/orders/my-orders")
                                .param("page", "1")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.data[0].id").value(100));
        }

        @Test
        @DisplayName("Cập nhật trạng thái đơn hàng thành công")
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        void updateStatus_Success() throws Exception {
                OrderResponse updatedResponse = OrderResponse.builder()
                                .id(100L)
                                .customerName("Test Customer")
                                .totalAmount(BigDecimal.valueOf(5000))
                                .status("COMPLETED")
                                .build();

                Mockito.when(orderService.updateOrderStatus(eq(100L), eq("COMPLETED"))).thenReturn(updatedResponse);

                mockMvc.perform(put("/api/orders/100/status")
                                .param("status", "COMPLETED")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.status").value("COMPLETED"));
        }
}

package com.wims.backend.mapper;

import com.wims.backend.dto.response.OrderDetailResponse;
import com.wims.backend.dto.response.OrderResponse;
import com.wims.backend.entity.Order;
import com.wims.backend.entity.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Để Spring tự Inject được (Autowired)
public interface OrderMapper {

    // 1. Map từ Order Entity -> OrderResponse
    @Mapping(target = "user", source = "user") // Map user entity sang user response
    OrderResponse toOrderResponse(Order order);

    // 2. Map từ OrderDetail Entity -> OrderDetailResponse
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name") // Lấy tên sản phẩm thẳng vào DTO
    @Mapping(target = "productImage", source = "product.image")
    @Mapping(target = "isDiscounted", source = "discounted")
    OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);

    // MapStruct đủ thông minh để tự động Map List<OrderDetail> sang List<OrderDetailResponse>
    // nhờ vào hàm số 2 ở trên.
}
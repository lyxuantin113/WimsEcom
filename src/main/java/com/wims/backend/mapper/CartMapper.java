package com.wims.backend.mapper;

import com.wims.backend.dto.response.CartResponse;
import com.wims.backend.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class}) // uses để nó biết dùng CartItemMapper cho list con
public interface CartMapper {
    @Mapping(target = "items", source = "cartItems") // Map list entity sang list response
    CartResponse toCartResponse(Cart cart);
}

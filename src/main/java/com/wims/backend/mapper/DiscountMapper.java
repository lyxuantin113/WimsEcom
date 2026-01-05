package com.wims.backend.mapper;

import com.wims.backend.dto.request.DiscountRequest;
import com.wims.backend.dto.response.DiscountResponse;
import com.wims.backend.entity.Discount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DiscountMapper {
    Discount toDiscount(DiscountRequest request);
    DiscountResponse toDiscountResponse(Discount discount);
    void updateDiscount(@MappingTarget Discount discount, DiscountRequest request);
}
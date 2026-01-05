package com.wims.backend.mapper;

import com.wims.backend.dto.request.ProductRequestDTO;
import com.wims.backend.dto.response.ProductResponse;
import com.wims.backend.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toProduct(ProductRequestDTO request);

    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "category", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductRequestDTO request);
}
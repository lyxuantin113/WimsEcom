package com.wims.backend.mapper;

import com.wims.backend.dto.request.CategoryRequestDTO;
import com.wims.backend.dto.response.CategoryResponse;
import com.wims.backend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequestDTO categoryRequestDTO);
    CategoryResponse toCategoryResponse(Category category);
}

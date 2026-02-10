package com.wims.backend.service.based;

import com.wims.backend.dto.request.CategoryRequestDTO;
import com.wims.backend.dto.response.CategoryResponse;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.entity.Category;
import com.wims.backend.exception.AppException;
import com.wims.backend.mapper.CategoryMapper;
import com.wims.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse createCategory(CategoryRequestDTO request) {
        Category category = categoryMapper.toCategory(request);
        category = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(category);
    }

    public PageResponse<CategoryResponse> getAll(int page, int size, String sortBy) {
        Sort sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        Page<CategoryResponse> categoryResponsePage = categoryPage.map(categoryMapper::toCategoryResponse);

        return PageResponse.<CategoryResponse>builder()
                .currentPage(page)
                .totalPages(categoryResponsePage.getTotalPages())
                .pageSize(categoryResponsePage.getSize())
                .totalElements(categoryResponsePage.getTotalElements())
                .data(categoryResponsePage.getContent())
                .build();
    }

    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Danh mục không tồn tại"));

        return categoryMapper.toCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequestDTO request) {
        // 1. Kiểm tra tồn tại
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Danh mục không tồn tại"));

        // 2. Cập nhật thông tin (Dùng Mapper update hoặc Set thủ công)
        // Cách 1: Set thủ công
        category.setName(request.getName());

        // Cách 2: Nếu dùng MapStruct nâng cao (@MappingTarget)
        // categoryMapper.updateCategory(category, request);

        // 3. Lưu lại
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(1001, "Danh mục không tồn tại"));
        categoryRepository.delete(category);
    }
}
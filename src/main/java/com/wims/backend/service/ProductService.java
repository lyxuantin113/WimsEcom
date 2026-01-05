package com.wims.backend.service;

import com.wims.backend.dto.request.ProductRequestDTO;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.dto.response.ProductResponse;
import com.wims.backend.entity.Category;
import com.wims.backend.entity.Product;
import com.wims.backend.exception.AppException;
import com.wims.backend.mapper.ProductMapper;
import com.wims.backend.repository.CategoryRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.repository.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service // Đánh dấu đây là nơi xử lý nghiệp vụ
@RequiredArgsConstructor
public class ProductService {

    // DI: Tiêm Repository vào để dùng
    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final CategoryRepository categoryRepository;

    private final FileStorageService fileStorageService;

    // Để xử lý triệt để Self-Invocation
    private final TransactionTemplate transactionTemplate;

    // Hàm lấy tất cả sản phẩm
    public PageResponse<ProductResponse> getAllProducts(
            int page, int size, String sortBy,
            String keyword, BigDecimal minPrice, BigDecimal maxPrice, boolean isOutOfStock, Long categoryId
    ) {
        // 1. Tạo đối tượng Pageable
        // Logic: Sắp xếp theo SortBy (Mặc định) giảm dần (Sản phẩm mới nhất lên đầu)
        // pageable trả về như 1 query Trả về "danh sách các Product" nhưng "có điều kiện"
        Sort sort = Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // 2. KHỞI TẠO SPECIFICATION (Đây là phần quan trọng nhất)
        // Specification.where(null) nghĩa là "không có điều kiện gì cả" (SELECT * FROM products)
        Specification<Product> spec = Specification.where(null);

        // 3. Lắp ráp từng mảnh Lego
        // Nếu user có gửi keyword -> Nối thêm điều kiện lọc tên
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(ProductSpecification.hasName(keyword));
        }

        // Nếu user có gửi khoảng giá -> Nối thêm điều kiện lọc giá
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecification.hasPriceRange(minPrice, maxPrice));
        }

        // Lọc các Products hết hàng
        if (isOutOfStock) {
            spec = spec.and(ProductSpecification.isOutOfStock(isOutOfStock));
        }

        // Lọc theo Cat
        if (categoryId != null && categoryId > 0) {
            spec = spec.and(ProductSpecification.hasCategory(categoryId));
        }

        // 4. Gọi Repository với CẢ HAI tham số: spec (điều kiện WHERE) và pageable (LIMIT/OFFSET)
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // 5. Map dữ liệu (Dùng cách Clean Code ta vừa học)
        Page<ProductResponse> dtoPage = productPage.map(productMapper::toProductResponse);

        return PageResponse.<ProductResponse>builder()
                .currentPage(page)
                .pageSize(dtoPage.getSize())
                .totalPages(dtoPage.getTotalPages())
                .totalElements(dtoPage.getTotalElements())
                .data(dtoPage.getContent())
                .build();
    }

    public ProductResponse getProductById(Long id) {
        // 1. Tìm sản phẩm, nếu không thấy thì NÉM LỖI NGAY (để Controller trả về 404)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(1003, "Sản phẩm không tồn tại với id: " + id));

        // 2. Map sang DTO
        return productMapper.toProductResponse(product);
    }

    // Hàm tạo sản phẩm mới
    public ProductResponse createProduct(ProductRequestDTO request) {
        // 1. Validate: Kiểm tra xem mã sản phẩm đã tồn tại chưa
        if (productRepository.findByCode(request.getCode()).isPresent()) {
            throw new AppException(1001, "Sản phẩm mã " + request.getCode() + " đã tồn tại!");
        }

        // 2. Kiểm tra và lưu file trước khi chạm đến DB
        String imageUrl = null;

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            try {
                imageUrl = fileStorageService.uploadImage(request.getFile());
            } catch (IOException e) {
                throw new AppException(9999, "Lỗi upload ảnh: " + e.getMessage());
            }
        }

        // Biến dùng trong lambda phải là final hoặc effectively final
        String finalImageUrl = imageUrl;

        return transactionTemplate.execute(status -> {
            // Mọi code trong block này đều nằm trong Transaction
            // Nếu lỗi -> Tự Rollback.
            return saveProductToDB(finalImageUrl, request);
        });
    }

    // Transaction không dùng cho private nhưng đã có TransactionTemplate nên dùng private được
    private ProductResponse saveProductToDB(String imageUrl, ProductRequestDTO request){

        Product product = productMapper.toProduct(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(1004, "Danh mục không tồn tại"));

        // Gán danh mục cho sản phẩm
        product.setCategory(category);
        product.setImage(imageUrl);
        product = productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    // Sửa sản phẩm
    public ProductResponse updateProduct(Long id, ProductRequestDTO request) {
        // 1. Tìm sản phẩm
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(1003, "Không tìm thấy sản phẩm id: " + id));

        // 2. --- LOGIC ẢNH MỚI ---
        String oldImageUrl = product.getImage();

        String newImageUrl = null;
        boolean hasNewImageUrl = request.getFile() != null && !request.getFile().isEmpty();

        if (hasNewImageUrl) {
            try {
                newImageUrl = fileStorageService.uploadImage(request.getFile());

            } catch (IOException e) {
                throw new AppException(9999, "Lỗi upload ảnh mới: " + e.getMessage());
            }
        }

        String finalNewImageUrl = newImageUrl;

        ProductResponse response = transactionTemplate.execute(status -> {
            // Mọi code trong block này đều nằm trong Transaction
            // Nếu lỗi -> Tự Rollback.
            return updateProductToDB(finalNewImageUrl, product, request);
        });

        if (hasNewImageUrl && oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try {
                fileStorageService.deleteImage(product.getImage());
            } catch (IOException e) {
                throw new AppException(9999, "Lỗi xóa ảnh cũ: " + e.getMessage());
            }
        }

        return response;
    }

    private ProductResponse updateProductToDB(String newImageUrl, Product product, ProductRequestDTO request) {
        // 3. Map các thông tin khác (Tên, giá...)
        productMapper.updateProduct(product, request);

        if (newImageUrl != null) {
            product.setImage(newImageUrl);
        }

        // 4. Cập nhật Category nếu có thay đổi
        if(request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(1004, "Danh mục không tồn tại"));
            product.setCategory(category);
        }

        return productMapper.toProductResponse(productRepository.save(product));
    }

    // Xóa sản phẩm
    public void deleteProduct(Long id) {
        // 1. Tìm sản phẩm (Check tồn tại)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(1003, "Không tìm thấy sản phẩm id: " + id));

        // 2. Xóa ảnh trong ổ cứng (NẾU CÓ) - Logic mới thêm
        // Xóa ảnh trên Cloud để tiết kiệm dung lượng
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                fileStorageService.deleteImage(product.getImage());
            } catch (IOException e) {
                // Nếu xóa ảnh trên cloud lỗi thì log ra thôi, vẫn cho xóa DB
                System.err.println("Không xóa được ảnh trên cloud: " + e.getMessage());
            }
        }
        // ---------------------

        // 3. Xóa dữ liệu trong DB
        productRepository.deleteById(id);
    }

    public List<ProductResponse> getRelatedProducts(Long currentProductId) {
        // 1. Lấy thông tin sản phẩm hiện tại để biết nó thuộc Category nào
        Product currentProduct = productRepository.findById(currentProductId)
                .orElseThrow(() -> new AppException(1003, "Sản phẩm không tồn tại"));

        // 2. Lấy 4 sản phẩm cùng danh mục (trừ chính nó)
        Pageable pageable = PageRequest.of(0, 4); // Lấy 4 cái đầu tiên
        Page<Product> products = productRepository.findByCategoryIdAndIdNot(
                currentProduct.getCategory().getId(),
                currentProductId,
                pageable
        );

        // 3. Map sang Response
        return products.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }
}
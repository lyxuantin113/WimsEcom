package com.wims.backend.service.based;

import com.wims.backend.dto.request.ProductRequestDTO;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.dto.response.ProductResponse;
import com.wims.backend.entity.Category;
import com.wims.backend.entity.Product;
import com.wims.backend.exception.AppException;
import com.wims.backend.mapper.ProductMapper;
import com.wims.backend.repository.CategoryRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.service.featured.FileStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private Category mockCategory;
    private ProductRequestDTO mockRequest;

    @BeforeEach
    void setUp() {
        mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setName("Laptops");

        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("MacBook Pro");
        mockProduct.setCode("MACB01");
        mockProduct.setPrice(BigDecimal.valueOf(2000));
        mockProduct.setStockQuantity(10);
        mockProduct.setCategory(mockCategory);

        mockRequest = new ProductRequestDTO(
                "MACB01",
                "MacBook Pro",
                null,
                BigDecimal.valueOf(2000),
                10,
                null,
                1L);
    }

    @Test
    @DisplayName("Lấy danh sách sản phẩm thành công (Có phân trang)")
    void getAllProducts_Success() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(List.of(mockProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        ProductResponse responseMock = ProductResponse.builder().id(100L).build();
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(responseMock);

        // Act
        PageResponse<ProductResponse> result = productService.getAllProducts(
                1, 10, "id", "MacBook", null, null, false, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals(100L, result.data().get(0).id());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID thành công")
    void getProductById_Success() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProduct));
        ProductResponse responseMock = ProductResponse.builder()
                .id(100L)
                .name("MacBook Pro")
                .build();
        when(productMapper.toProductResponse(any(Product.class))).thenReturn(responseMock);

        // Act
        ProductResponse result = productService.getProductById(100L);

        // Assert
        assertNotNull(result);
        assertEquals("MacBook Pro", result.name());
        assertEquals(100L, result.id());
        verify(productRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID - Bắn Exception lỗi 1004 nếu không tìm thấy")
    void getProductById_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            productService.getProductById(999L);
        });

        assertEquals(1004, exception.getErrorCode());
        assertEquals("Sản phẩm không tồn tại với id: 999", exception.getMessage());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Tạo mới sản phẩm - Exception Code Trùng")
    void createProduct_CodeAlreadyExists() {
        // Arrange
        when(productRepository.findByCode(anyString())).thenReturn(Optional.of(mockProduct));

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            productService.createProduct(mockRequest);
        });

        assertEquals(1004, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("đã tồn tại"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo mới sản phẩm thành công với hình ảnh upload")
    void createProduct_SuccessWithImage() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        mockRequest = new ProductRequestDTO(
                "MACB01",
                "MacBook Pro",
                null,
                BigDecimal.valueOf(2000),
                10,
                mockFile,
                1L);

        when(productRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(fileStorageService.uploadImage(any())).thenReturn("http://cloudinary.com/image.jpg");

        // Mock TransactionTemplate
        ProductResponse responseMock = ProductResponse.builder()
                .id(100L)
                .image("http://cloudinary.com/image.jpg")
                .build();
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<ProductResponse> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        when(productMapper.toProduct(any())).thenReturn(mockProduct);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any())).thenReturn(mockProduct);
        when(productMapper.toProductResponse(any())).thenReturn(responseMock);

        // Act
        ProductResponse result = productService.createProduct(mockRequest);

        // Assert
        assertNotNull(result);
        assertEquals("http://cloudinary.com/image.jpg", result.image());
        verify(fileStorageService, times(1)).uploadImage(mockFile);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Xóa sản phẩm - Exception không tồn tại")
    void deleteProduct_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> productService.deleteProduct(999L));
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Xóa sản phẩm thành công và xóa luôn ảnh")
    void deleteProduct_Success() throws IOException {
        mockProduct.setImage("http://cloudinary.com/image.jpg");
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProduct));

        productService.deleteProduct(100L);

        verify(fileStorageService, times(1)).deleteImage("http://cloudinary.com/image.jpg");
        verify(productRepository, times(1)).deleteById(100L);
    }
}

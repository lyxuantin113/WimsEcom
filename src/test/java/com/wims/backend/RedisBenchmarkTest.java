package com.wims.backend;

import com.wims.backend.entity.Product;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.service.based.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest // Dùng SpringBootTest để chạy cả Context thật (DB thật, Redis thật)
public class RedisBenchmarkTest {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CacheManager cacheManager;

    @BeforeEach
    void setup() {
        // 1. Xóa cache cũ để test công bằng
        if (cacheManager.getCache("products") != null) {
            cacheManager.getCache("products").clear();
        }

        // 2. Tạo dữ liệu giả số lượng lớn (Chỉ tạo nếu DB trống)
        if (productRepository.count() < 1000) {
            System.out.println("--- ĐANG TẠO DỮ LIỆU GIẢ (2000 PRODUCTS) ---");
            List<Product> dummyProducts = new ArrayList<>();
            for (int i = 0; i < 2000; i++) {
                Product p = new Product();
                p.setName("Product " + i);
                p.setPrice(BigDecimal.valueOf(1000 + i));
                p.setCode("P" + i);
                p.setDeleted(false);
                dummyProducts.add(p);
            }
            productRepository.saveAll(dummyProducts);
            System.out.println("--- ĐÃ TẠO XONG ---");
        }
    }

    @Test
    void comparePerformance() {
        int loops = 10; // Số lần lặp lại request

        // Các tham số giả lập (Lấy trang 1, 1000 phần tử, không lọc gì cả)
        int page = 1;
        int size = 1000;
        String sortBy = "id";
        String keyword = "";
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        boolean isOutOfStock = false;
        Long categoryId = null;

        // --- ROUND 1: KHÔNG CACHE (GỌI REPO) ---
        long startNoCache = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            // Lưu ý: Repository findAll cũng cần Pageable nếu bạn dùng PagingAndSortingRepository
            // Nếu lười viết Pageable ở đây, bạn có thể gọi tạm findAll() không tham số của JpaRepository
            // để đo tốc độ Raw DB (như code cũ).
            productRepository.findAll();
        }
        long endNoCache = System.currentTimeMillis();
        long timeNoCache = endNoCache - startNoCache;


        // --- ROUND 2: CÓ CACHE (GỌI SERVICE) ---

        // Gọi mồi lần đầu (Để nó lưu vào Redis)
        productService.getAllProducts(page, size, sortBy, keyword, minPrice, maxPrice, isOutOfStock, categoryId);

        long startWithCache = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            // Gọi y hệt lần đầu -> Sẽ trúng Cache (Cache Hit)
            productService.getAllProducts(page, size, sortBy, keyword, minPrice, maxPrice, isOutOfStock, categoryId);
        }
        long endWithCache = System.currentTimeMillis();
        long timeWithCache = endWithCache - startWithCache;

        // --- KẾT QUẢ ---
        System.out.println("\n================ KẾT QUẢ BENCHMARK ================");
        System.out.println("Số lượng query thực hiện: " + loops + " lần");
        System.out.println("Tổng thời gian (Không Cache - MySQL): " + timeNoCache + " ms");
        System.out.println("Tổng thời gian (Có Redis Cache):      " + timeWithCache + " ms");

        if (timeWithCache > 0) {
            double speedUp = (double) timeNoCache / timeWithCache;
            System.out.printf("🚀 REDIS NHANH HƠN KHOẢNG: %.2f LẦN\n", speedUp);
        }
        System.out.println("===================================================\n");
    }
}
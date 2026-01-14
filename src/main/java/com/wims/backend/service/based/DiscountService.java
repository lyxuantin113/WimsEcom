package com.wims.backend.service.based;

import com.wims.backend.dto.request.CartItemRequest;
import com.wims.backend.dto.request.DiscountCalculationRequest;
import com.wims.backend.dto.request.DiscountRequest;
import com.wims.backend.dto.response.DiscountResponse;
import com.wims.backend.dto.response.DiscountCalculationResponse;
import com.wims.backend.entity.Discount;
import com.wims.backend.entity.Product;
import com.wims.backend.enums.DiscountScope;
import com.wims.backend.enums.DiscountType;
import com.wims.backend.exception.AppException;
import com.wims.backend.mapper.DiscountMapper;
import com.wims.backend.repository.DiscountRepository;
import com.wims.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    private final DiscountMapper discountMapper;

    // Hàm tính toán: Trả về số tiền ĐƯỢC GIẢM (Discount Amount)
    public DiscountCalculationResponse calculateDiscount(DiscountCalculationRequest request) {
        // 1. Tìm Voucher
        Discount discount = discountRepository.findByCodeAndActiveTrue(request.getCode())
                .orElseThrow(() -> new AppException(1004, "Mã giảm giá không tồn tại hoặc đã bị khóa"));

        // 2. Validate điều kiện cơ bản
        if (discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new AppException(1005, "Mã giảm giá đã hết lượt sử dụng");
        }
        if (LocalDateTime.now().isBefore(discount.getStartDate())) {
            throw new AppException(1006, "Mã giảm giá chưa đến đợt áp dụng");
        }
        if (LocalDateTime.now().isAfter(discount.getEndDate())) {
            throw new AppException(1007, "Mã giảm giá đã hết hạn");
        }

        BigDecimal totalOrderValue = BigDecimal.ZERO;
        BigDecimal eligibleAmount = BigDecimal.ZERO; // Tổng tiền các món được giảm

        // Chuẩn bị danh sách ID được phép giảm (nếu không phải GLOBAL)
        Set<Long> allowedIds = new HashSet<>();
        List<Long> actualAffectedIds = new ArrayList<>();

        if (discount.getScope() != DiscountScope.GLOBAL && discount.getApplicableIds() != null) {
            Arrays.stream(discount.getApplicableIds().split(","))
                    .map(Long::parseLong)
                    .forEach(allowedIds::add);
        }

        List<Long> productIds = request.getItems().stream()
                .map(CartItemRequest::getProductId)
                .toList();

        List<Product> productList = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 3. Duyệt qua từng món hàng trong giỏ
        for (CartItemRequest itemReq : request.getItems()) {
            // 🔥 Query DB để lấy giá chuẩn và Category
            Product product = productMap.get(itemReq.getProductId());

            if (product == null) {
                throw new AppException(1004, "Sản phẩm không tồn tại: " + itemReq.getProductId());
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalOrderValue = totalOrderValue.add(lineTotal);

            // Check xem món này có được giảm giá không
            boolean isEligible = false;
            if (discount.getScope() == DiscountScope.GLOBAL) {
                isEligible = true;
            } else if (discount.getScope() == DiscountScope.SPECIFIC_PRODUCT) {
                if (allowedIds.contains(product.getId())) isEligible = true;
            } else if (discount.getScope() == DiscountScope.SPECIFIC_CATEGORY) {
                // Giả sử Product có quan hệ ManyToOne với Category
                if (allowedIds.contains(product.getCategory().getId())) isEligible = true;
            }

            if (isEligible) {
                eligibleAmount = eligibleAmount.add(lineTotal);
                actualAffectedIds.add(product.getId());
            }
        }

        // 4. Validate giá trị đơn tối thiểu
        if (totalOrderValue.compareTo(discount.getMinOrderValue()) < 0) {
            throw new AppException(1008, "Đơn hàng chưa đạt tối thiểu: " + discount.getMinOrderValue());
        }

        if (eligibleAmount.compareTo(BigDecimal.ZERO) == 0) {
            // Nếu là voucher global thì eligibleAmount chính là totalOrderValue
            // Nếu voucher sản phẩm mà không món nào khớp thì trả về 0
            if (discount.getScope() != DiscountScope.GLOBAL){
                throw new AppException(1009, "Mã này không áp dụng cho sản phẩm nào trong giỏ của bạn");
            }

            eligibleAmount = totalOrderValue;
        }

        // 5. Tính toán tiền giảm
        return calculateAmountByType(discount, totalOrderValue, eligibleAmount, actualAffectedIds);
    }

    // Hàm phụ: Tính toán số tiền giảm dựa trên Type (FIXED hay PERCENTAGE)
    private DiscountCalculationResponse calculateAmountByType(Discount discount, BigDecimal totalOrderValue, BigDecimal baseAmount, List<Long> actualAffectedIds) {
        BigDecimal result;
        if (discount.getType() == DiscountType.FIXED_AMOUNT) {
            result = totalOrderValue.compareTo(discount.getValue()) < 0 ? totalOrderValue : discount.getValue();
        } else {
            // Tính %: baseAmount * value / 100
            result = baseAmount.multiply(discount.getValue()).divide(BigDecimal.valueOf(100));
            // Check Cap (Giảm tối đa)
            if (discount.getMaxDiscountAmount() != null) {
                result = result.min(discount.getMaxDiscountAmount());
            }
        }

        return DiscountCalculationResponse.builder().totalDiscount(result).affectedProductIds(actualAffectedIds).build();
    }

    // 1. Lấy tất cả (Admin xem)
    public List<DiscountResponse> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(discountMapper::toDiscountResponse)
                .collect(Collectors.toList());
    }

    // 2. Tạo mới
    public DiscountResponse createDiscount(DiscountRequest request) {
        if (discountRepository.existsByCode(request.getCode())) {
            throw new AppException(1001, "Mã giảm giá đã tồn tại");
        }

        // Validate ngày tháng logic
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(1002, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        Discount discount = discountMapper.toDiscount(request);
        discount.setUsedCount(0); // Mặc định chưa dùng

        return discountMapper.toDiscountResponse(discountRepository.save(discount));
    }

    // 3. Cập nhật
    public DiscountResponse updateDiscount(Long id, DiscountRequest request) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Voucher không tồn tại"));

        // Nếu admin đổi code, phải check xem code mới có trùng ai không (trừ chính nó)
        // Ở đây để đơn giản ta map thẳng, nếu trùng database constraint sẽ báo lỗi hoặc ta check thủ công

        discountMapper.updateDiscount(discount, request);
        return discountMapper.toDiscountResponse(discountRepository.save(discount));
    }

    // 4. Xóa
    public void deleteDiscount(Long id) {
        if (!discountRepository.existsById(id)) {
            throw new AppException(1004, "Voucher không tồn tại");
        }
        discountRepository.deleteById(id);
    }
}

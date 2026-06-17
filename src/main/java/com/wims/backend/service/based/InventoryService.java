package com.wims.backend.service.based;

import com.wims.backend.entity.InventoryTransaction;
import com.wims.backend.entity.Product;
import com.wims.backend.enums.TransactionType;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.InventoryTransactionRepository;
import com.wims.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import com.wims.backend.dto.response.InventoryTransactionResponse;
import com.wims.backend.dto.response.PageResponse;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    @Transactional
    public void importStock(Long productId, Integer quantity, Long referenceId, String note) {
        processTransaction(productId, quantity, TransactionType.IMPORT, referenceId, note);
    }

    @Transactional
    public void exportStock(Long productId, Integer quantity, Long referenceId, String note) {
        processTransaction(productId, quantity, TransactionType.EXPORT, referenceId, note);
    }

    @Transactional
    public void returnStock(Long productId, Integer quantity, Long referenceId, String note) {
        processTransaction(productId, quantity, TransactionType.RETURN, referenceId, note);
    }

    private void processTransaction(Long productId, Integer quantity, TransactionType type, Long referenceId,
            String note) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(1004, "Sản phẩm không tồn tại: " + productId));

        if (quantity <= 0) {
            throw new AppException(400, "Số lượng giao dịch kho phải lớn hơn 0");
        }

        int currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();

        if (type == TransactionType.EXPORT) {
            if (currentStock < quantity) {
                throw new AppException(1005, "Sản phẩm " + product.getName() + " đã hết hàng hoặc không đủ số lượng.");
            }
            product.setStockQuantity(currentStock - quantity);
        } else {
            // IMPORT, RETURN, ADJUSTMENT (tăng kho)
            product.setStockQuantity(currentStock + quantity);
        }

        productRepository.save(product);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(type);
        transaction.setQuantity(type == TransactionType.EXPORT ? -quantity : quantity);
        transaction.setReferenceId(referenceId);
        transaction.setNote(note);

        transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryTransactionResponse> getAllTransactions(
            int page, int size, LocalDateTime startDate, LocalDateTime endDate, String keyword) {

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Specification<InventoryTransaction> spec = Specification.where(null);

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<InventoryTransaction, Product> productJoin = root.join("product");
                return cb.or(
                        cb.like(cb.lower(productJoin.get("name")), "%" + keyword.toLowerCase() + "%"),
                        cb.like(cb.lower(productJoin.get("code")), "%" + keyword.toLowerCase() + "%"));
            });
        }

        Page<InventoryTransaction> pageData = transactionRepository.findAll(spec, pageable);

        Page<InventoryTransactionResponse> dtoPage = pageData.map(tx -> InventoryTransactionResponse.builder()
                .id(tx.getId())
                .productId(tx.getProduct().getId())
                .productCode(tx.getProduct().getCode())
                .productName(tx.getProduct().getName())
                .quantity(tx.getQuantity())
                .transactionType(tx.getTransactionType())
                .referenceId(tx.getReferenceId())
                .note(tx.getNote())
                .createdAt(tx.getCreatedAt())
                .build());

        return PageResponse.<InventoryTransactionResponse>builder()
                .currentPage(page)
                .pageSize(dtoPage.getSize())
                .totalPages(dtoPage.getTotalPages())
                .totalElements(dtoPage.getTotalElements())
                .data(dtoPage.getContent())
                .build();
    }
}

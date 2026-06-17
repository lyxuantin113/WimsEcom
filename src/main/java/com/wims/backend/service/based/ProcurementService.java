package com.wims.backend.service.based;

import com.wims.backend.dto.request.ProcurementItemRequest;
import com.wims.backend.dto.request.ProcurementRequest;
import com.wims.backend.entity.Procurement;
import com.wims.backend.entity.ProcurementItem;
import com.wims.backend.entity.Product;
import com.wims.backend.entity.Supplier;
import com.wims.backend.entity.User;
import com.wims.backend.enums.ProcurementStatus;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.dto.response.ProcurementItemResponse;
import com.wims.backend.dto.response.ProcurementResponse;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.ProcurementItemRepository;
import com.wims.backend.repository.ProcurementRepository;
import com.wims.backend.repository.ProductRepository;
import com.wims.backend.repository.SupplierRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.wims.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcurementService {

    private final ProcurementRepository procurementRepository;
    private final ProcurementItemRepository procurementItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;

    @Transactional
    public Procurement createDraftProcurement(ProcurementRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new AppException(1004, "Nhà cung cấp không tồn tại"));

        List<Long> productIds = request.items().stream()
                .map(ProcurementItemRequest::productId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (products.size() != productIds.size()) {
            throw new AppException(1004, "Một số sản phẩm không tồn tại");
        }

        Procurement procurement = new Procurement();
        procurement.setSupplier(supplier);
        procurement.setStatus(ProcurementStatus.DRAFT);
        procurement.setNote(request.note());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ProcurementItem> details = new ArrayList<>();

        for (ProcurementItemRequest reqItem : request.items()) {
            Product product = productMap.get(reqItem.productId());

            ProcurementItem item = new ProcurementItem();
            item.setProcurement(procurement);
            item.setProduct(product);
            item.setQuantity(reqItem.quantity());
            item.setUnitPrice(reqItem.unitPrice());

            details.add(item);

            totalAmount = totalAmount.add(reqItem.unitPrice().multiply(BigDecimal.valueOf(reqItem.quantity())));
        }

        procurement.setTotalAmount(totalAmount);

        Procurement savedProcurement = procurementRepository.save(procurement);
        procurementItemRepository.saveAll(details);

        return savedProcurement;
    }

    @Transactional
    public void approveProcurement(Long procurementId) {
        User admin = securityUtils.getCurrentUserLogin();

        Procurement procurement = procurementRepository.findById(procurementId)
                .orElseThrow(() -> new AppException(1004, "Phiếu nhập không tồn tại"));

        if (procurement.getStatus() != ProcurementStatus.DRAFT) {
            throw new AppException(1009, "Phiếu nhập này không ở trạng thái nháp");
        }

        procurement.setStatus(ProcurementStatus.APPROVED);
        procurement.setApprovedAt(LocalDateTime.now());
        procurement.setApprovedBy(admin);

        procurementRepository.save(procurement);

        List<ProcurementItem> items = procurementItemRepository.findByProcurementId(procurementId);

        for (ProcurementItem item : items) {
            inventoryService.importStock(item.getProduct().getId(), item.getQuantity(), procurement.getId(),
                    "Nhập kho theo phiếu " + procurement.getId());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ProcurementResponse> getAllProcurements(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Procurement> procurementPage = procurementRepository.findAll(pageable);

        Page<ProcurementResponse> dtoPage = procurementPage.map(this::mapToResponse);

        return PageResponse.<ProcurementResponse>builder()
                .currentPage(page)
                .pageSize(dtoPage.getSize())
                .totalPages(dtoPage.getTotalPages())
                .totalElements(dtoPage.getTotalElements())
                .data(dtoPage.getContent())
                .build();
    }

    @Transactional(readOnly = true)
    public ProcurementResponse getProcurementById(Long id) {
        Procurement procurement = procurementRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Phiếu nhập không tồn tại"));

        List<ProcurementItem> items = procurementItemRepository.findByProcurementId(id);

        ProcurementResponse response = mapToResponse(procurement);
        response.setItems(items.stream().map(item -> ProcurementItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productCode(item.getProduct().getCode())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build()).collect(Collectors.toList()));

        return response;
    }

    private ProcurementResponse mapToResponse(Procurement procurement) {
        return ProcurementResponse.builder()
                .id(procurement.getId())
                .supplier(procurement.getSupplier())
                .status(procurement.getStatus())
                .totalAmount(procurement.getTotalAmount())
                .note(procurement.getNote())
                .approvedAt(procurement.getApprovedAt())
                .approvedByUsername(
                        procurement.getApprovedBy() != null ? procurement.getApprovedBy().getUsername() : null)
                .createdAt(procurement.getCreatedAt())
                .updatedAt(procurement.getUpdatedAt())
                .build();
    }
}

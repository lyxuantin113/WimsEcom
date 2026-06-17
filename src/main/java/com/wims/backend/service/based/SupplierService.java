package com.wims.backend.service.based;

import com.wims.backend.entity.Supplier;
import com.wims.backend.exception.AppException;
import com.wims.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll(); // Có thể sau này đổi thành findAllByActiveTrue
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Nhà cung cấp không tồn tại"));
    }

    @Transactional
    public Supplier createSupplier(Supplier request) {
        // Có thể bổ sung check trùng email/phone nếu cần
        return supplierRepository.save(request);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Nhà cung cấp không tồn tại"));

        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setActive(request.isActive());

        return supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Nhà cung cấp không tồn tại"));

        // Soft delete
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }
}

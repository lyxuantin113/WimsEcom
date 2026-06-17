package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.entity.Supplier;
import com.wims.backend.repository.SupplierRepository;
import com.wims.backend.service.based.SupplierService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Supplier>> getAllSuppliers() {
        return ApiResponse.success(supplierService.getAllSuppliers()).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Supplier> getSupplier(@PathVariable Long id) {
        return ApiResponse.success(supplierService.getSupplierById(id)).build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Supplier> createSupplier(@RequestBody Supplier supplier) {
        return ApiResponse.success(supplierService.createSupplier(supplier)).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        return ApiResponse.success(supplierService.updateSupplier(id, supplier)).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ApiResponse.success("Đã xóa (mềm) nhà cung cấp thành công!").build();
    }
}

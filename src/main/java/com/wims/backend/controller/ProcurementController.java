package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.ProcurementRequest;
import com.wims.backend.entity.Procurement;
import com.wims.backend.service.based.ProcurementService;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.dto.response.ProcurementResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/procurements")
@RequiredArgsConstructor
public class ProcurementController {

    private final ProcurementService procurementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Procurement> createDraftProcurement(@RequestBody @Valid ProcurementRequest request) {
        return ApiResponse.success(procurementService.createDraftProcurement(request)).build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> approveProcurement(@PathVariable Long id) {
        procurementService.approveProcurement(id);
        return ApiResponse.success("Duyệt phiếu nhập kho thành công!").build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ProcurementResponse>> getAllProcurements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(procurementService.getAllProcurements(page, size)).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProcurementResponse> getProcurement(@PathVariable Long id) {
        return ApiResponse.success(procurementService.getProcurementById(id)).build();
    }
}

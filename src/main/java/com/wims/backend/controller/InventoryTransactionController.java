package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.response.InventoryTransactionResponse;
import com.wims.backend.dto.response.PageResponse;
import com.wims.backend.service.based.InventoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class InventoryTransactionController {

    private final InventoryService inventoryService;

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<InventoryTransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String keyword) {

        return ApiResponse.success(inventoryService.getAllTransactions(page, size, startDate, endDate, keyword))
                .build();
    }
}

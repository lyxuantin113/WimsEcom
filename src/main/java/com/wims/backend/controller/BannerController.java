package com.wims.backend.controller;

import com.wims.backend.dto.ApiResponse;
import com.wims.backend.dto.request.BannerRequest;
import com.wims.backend.dto.response.BannerResponse;
import com.wims.backend.service.based.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {
    private final BannerService bannerService;

    // API Public: Ai cũng gọi được (để hiện lên trang chủ)
    @GetMapping
    public ApiResponse<List<BannerResponse>> getActiveBanners() {
        return ApiResponse.success(bannerService.getAllActiveBanners()).build();
    }

    // --- CÁC API DƯỚI ĐÂY CẦN QUYỀN ADMIN (Config trong Security) ---

    @GetMapping("/admin")
    public ApiResponse<List<BannerResponse>> getAllBanners() {
        return ApiResponse.success(bannerService.getAllBannersForAdmin()).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BannerResponse> createBanner(@ModelAttribute BannerRequest request) {
        return ApiResponse.success(bannerService.createBanner(request)).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> updateBanner(@PathVariable Long id, @ModelAttribute BannerRequest request) {
        return ApiResponse.success(bannerService.updateBanner(id, request)).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ApiResponse.success("Deleted successfully").build();
    }
}
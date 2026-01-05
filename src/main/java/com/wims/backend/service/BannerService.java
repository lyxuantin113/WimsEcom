package com.wims.backend.service;

import com.wims.backend.dto.request.BannerRequest;
import com.wims.backend.dto.response.BannerResponse;
import com.wims.backend.entity.Banner;
import com.wims.backend.exception.AppException;
import com.wims.backend.mapper.BannerMapper;
import com.wims.backend.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;

    private final FileStorageService fileStorageService;

    // --- PUBLIC (Cho trang chủ) ---
    public List<BannerResponse> getAllActiveBanners() {
        // Lấy tất cả banner đang active = true, sắp xếp theo priority
        List<Banner> banners = bannerRepository.findAllByActiveTrueOrderByPriorityAsc();
        return banners.stream()
                .map(bannerMapper::toBannerResponse)
                .collect(Collectors.toList());
    }

    // --- ADMIN (Quản lý) ---
    public List<BannerResponse> getAllBannersForAdmin() {
        return bannerRepository.findAll().stream()
                .map(bannerMapper::toBannerResponse)
                .collect(Collectors.toList());
    }

    public BannerResponse createBanner(BannerRequest request) {

        Banner banner = bannerMapper.toBanner(request);

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            try {
                String imageUrl = fileStorageService.uploadImage(request.getFile());
                banner.setImageUrl(imageUrl); // Lưu link vào DB
            } catch (IOException e) {
                throw new AppException(9999, "Lỗi upload ảnh: " + e.getMessage());
            }
        }

        return bannerMapper.toBannerResponse(bannerRepository.save(banner));
    }

    public BannerResponse updateBanner(Long id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Banner not found"));

        String oldImageUrl = banner.getImageUrl();

        bannerMapper.updateBanner(banner, request);

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            try {
                // A. Xóa ảnh cũ trên Cloud (Nếu có)
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    fileStorageService.deleteImage(oldImageUrl);
                }

                // B. Up ảnh mới
                String newImageUrl = fileStorageService.uploadImage(request.getFile());
                banner.setImageUrl(newImageUrl);

            } catch (IOException e) {
                throw new AppException(9999, "Lỗi xử lý ảnh khi update: " + e.getMessage());
            }
        } else {
            banner.setImageUrl(oldImageUrl);
        }

        bannerMapper.updateBanner(banner, request);
        return bannerMapper.toBannerResponse(bannerRepository.save(banner));
    }

    public void deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new AppException(1004, "Banner not found"));

        try {
            if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
                fileStorageService.deleteImage(banner.getImageUrl());
            }
        } catch (IOException e) {
            throw new AppException(9999, "Lỗi xử lý ảnh khi update: " + e.getMessage());
        }

        bannerRepository.deleteById(id);
    }
}

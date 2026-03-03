package com.wims.backend.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record BannerRequest(
        MultipartFile file,
        String linkUrl,
        Integer priority,
        boolean active) {
}
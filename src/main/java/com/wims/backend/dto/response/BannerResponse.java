package com.wims.backend.dto.response;

import lombok.Builder;

@Builder
public record BannerResponse(
        Long id,
        String imageUrl,
        String linkUrl,
        Integer priority,
        boolean active) {
}

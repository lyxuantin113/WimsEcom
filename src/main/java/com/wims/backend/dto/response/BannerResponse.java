package com.wims.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BannerResponse {
    private Long id;
    private String imageUrl;
    private String linkUrl;
    private Integer priority;
    private boolean active;
}

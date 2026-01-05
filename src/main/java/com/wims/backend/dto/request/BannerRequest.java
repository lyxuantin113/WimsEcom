package com.wims.backend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BannerRequest {
    private MultipartFile file;
    private String linkUrl;
    private Integer priority;
    private boolean active;
}
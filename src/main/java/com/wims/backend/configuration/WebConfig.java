package com.wims.backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Đánh dấu đây là file cấu hình
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu trúc: Khi ai đó gọi đường dẫn /uploads/**
        // Thì trỏ nó vào thư mục "uploads" ở trong máy
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
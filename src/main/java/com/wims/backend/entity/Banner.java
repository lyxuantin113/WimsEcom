package com.wims.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;   // Link ảnh (Upload lên Cloudinary/Firebase)
    private String linkUrl;    // Link khi bấm vào banner (VD: /products/category/iphone)

    private Integer priority;  // Thứ tự hiển thị (1, 2, 3...)
    private boolean active;    // Ẩn/Hiện

    @CreationTimestamp
    private LocalDateTime createdAt;
}
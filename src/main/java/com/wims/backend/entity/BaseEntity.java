package com.wims.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@MappedSuperclass // Đánh dấu đây là class cha, không tạo bảng riêng trong DB
@EntityListeners(AuditingEntityListener.class) // Kích hoạt tính năng lắng nghe tự động
public abstract class BaseEntity {

    @CreatedDate // Tự động lấy thời gian hiện tại khi tạo mới
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // Tự động cập nhật thời gian khi sửa
    private LocalDateTime updatedAt;

    @CreatedBy // Tự động lấy tên user đang đăng nhập (sẽ cấu hình sau)
    private String createdBy;

    @LastModifiedBy // Tự động lấy tên user sửa cuối cùng
    private String updatedBy;
}
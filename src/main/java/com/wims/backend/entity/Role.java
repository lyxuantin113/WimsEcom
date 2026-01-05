package com.wims.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
// @NoArgsConstructor, @AllArgsConstructor nếu cần
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ví dụ: "ADMIN", "USER", "MANAGER"

    private String description; // Mô tả: "Quản trị viên hệ thống"
}

package com.wims.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Table(name = "categories")
@Data

@SQLDelete(sql = "UPDATE categories SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ví dụ: "Điện tử", "Gia dụng"

    @Column(name = "is_deleted")
    private boolean deleted = false;

    // Mối quan hệ 2 chiều (Optional): Một Category có list các Product
    // mappedBy = "category": Nghĩa là bên Product sẽ chịu trách nhiệm giữ khóa ngoại
    // FetchType.LAZY: Khi load Category, KHÔNG tự động load list products (để nhẹ máy)
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;
}
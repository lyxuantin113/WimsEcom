package com.wims.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
// Kế thừa BaseEntity để có ngày tạo, người tạo (Audit)
// Lưu ý: User extends BaseEntity thì User cũng sẽ có createdAt...
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Tên đăng nhập

    @Column(nullable = false)
    private String password; // Mật khẩu (Sẽ được mã hóa, không lưu text trần)

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;

    // Quan hệ Many-to-Many với Role
    // FetchType.EAGER: Khi load User, load luôn danh sách Role đi kèm (vì Role rất nhẹ)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "user_id"), // Khóa ngoại trỏ về User
            inverseJoinColumns = @JoinColumn(name = "role_id") // Khóa ngoại trỏ về Role
    )
    private Set<Role> roles = new HashSet<>();
}

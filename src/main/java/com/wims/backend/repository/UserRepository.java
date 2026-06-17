package com.wims.backend.repository;

import com.wims.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Để Login
    boolean existsByUsername(String username); // Để check trùng khi đăng ký

    // Đếm user đăng ký trong năm
    @Query("SELECT COUNT(u) FROM User u WHERE YEAR(u.createdAt) = :year")
    long countUsersByYear(@Param("year") int year);

    List<User> findByRoles_Name(String roleName);
}

package com.wims.backend.repository;

import com.wims.backend.entity.Procurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface ProcurementRepository extends JpaRepository<Procurement, Long> {
    @Query("SELECT SUM(p.totalAmount) FROM Procurement p WHERE p.status = 'APPROVED' AND p.approvedAt >= :startOfDay AND p.approvedAt <= :endOfDay")
    BigDecimal getDailyImportAmount(@org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay, @org.springframework.data.repository.query.Param("endOfDay") java.time.LocalDateTime endOfDay);

    @EntityGraph(attributePaths = {"supplier"})
    Page<Procurement> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"supplier"})
    Optional<Procurement> findById(Long id);
}

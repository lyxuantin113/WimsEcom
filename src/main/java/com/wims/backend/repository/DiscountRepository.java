package com.wims.backend.repository;

import com.wims.backend.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.QueryHints;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCodeAndActiveTrue(String code);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.QueryHints({
            @jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") })
    @org.springframework.data.jpa.repository.Query("SELECT d FROM Discount d WHERE d.code = :code AND d.active = true")
    Optional<Discount> findByCodeWithLock(@org.springframework.data.repository.query.Param("code") String code);

    boolean existsByCode(String code);
}

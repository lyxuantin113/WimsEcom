package com.wims.backend.repository;

import com.wims.backend.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCodeAndActiveTrue(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") })
    @Query("SELECT d FROM Discount d WHERE d.code = :code AND d.active = true")
    Optional<Discount> findByCodeWithLock(@Param("code") String code);

    boolean existsByCode(String code);
}

package com.wims.backend.repository;

import com.wims.backend.entity.ProcurementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProcurementItemRepository extends JpaRepository<ProcurementItem, Long> {
    List<ProcurementItem> findByProcurementId(Long procurementId);
}

package com.wims.backend.entity;

import com.wims.backend.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "inventory_transactions")
@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity; // Số lượng thay đổi (có thể dùng số tuyệt đối, dựa vào Type để biết +/-)

    @Column(name = "reference_id")
    private Long referenceId; // Chứa OrderId hoặc ProcurementId

    private String note;
}

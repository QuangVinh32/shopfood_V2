package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Voucher;
import com.example.shopfood.Model.Entity.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer>, JpaSpecificationExecutor<Voucher> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByStatus(VoucherStatus status);

    /**
     * Atomic increment usedCount với điều kiện chưa vượt limit.
     * Nếu thành công trả về 1, fail (do hết lượt) trả về 0.
     * Chống race condition khi 2 user dùng voucher đồng thời.
     */
    @Modifying
    @Query("""
        UPDATE Voucher v
           SET v.usedCount = v.usedCount + 1
         WHERE v.voucherId = :id
           AND (v.usageLimitGlobal IS NULL OR v.usedCount < v.usageLimitGlobal)
    """)
    int incrementUsedCountIfAvailable(@Param("id") Integer voucherId);

    @Modifying
    @Query("""
        UPDATE Voucher v
           SET v.usedCount = CASE WHEN v.usedCount > 0 THEN v.usedCount - 1 ELSE 0 END
         WHERE v.voucherId = :id
    """)
    int decrementUsedCount(@Param("id") Integer voucherId);
}

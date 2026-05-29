package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.UserVoucher;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {

    Optional<UserVoucher> findByUserAndVoucher(Users user, Voucher voucher);
    boolean existsByUserAndVoucher(Users user, Voucher voucher);
    List<UserVoucher> findAllByVoucher(Voucher voucher);
    void deleteByVoucher(Voucher voucher);

    /**
     * Atomic increment usedCount cho user-voucher nếu chưa vượt usageLimitPerUser.
     * Trả về 1 nếu thành công, 0 nếu hết lượt.
     */
    @Modifying
    @Query("""
        UPDATE UserVoucher uv
           SET uv.usedCount = uv.usedCount + 1,
               uv.lastUsedAt = CURRENT_TIMESTAMP
         WHERE uv.userVoucherId = :id
           AND (uv.voucher.usageLimitPerUser IS NULL
                OR uv.usedCount < uv.voucher.usageLimitPerUser)
    """)
    int incrementUsedCountIfAvailable(@Param("id") Long userVoucherId);

    @Modifying
    @Query("""
        UPDATE UserVoucher uv
           SET uv.usedCount = CASE WHEN uv.usedCount > 0 THEN uv.usedCount - 1 ELSE 0 END
         WHERE uv.userVoucherId = :id
    """)
    int decrementUsedCount(@Param("id") Long userVoucherId);
}


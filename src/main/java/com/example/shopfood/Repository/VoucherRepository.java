package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Voucher;
import com.example.shopfood.Model.Entity.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer>, JpaSpecificationExecutor<Voucher> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByStatus(VoucherStatus status);

}

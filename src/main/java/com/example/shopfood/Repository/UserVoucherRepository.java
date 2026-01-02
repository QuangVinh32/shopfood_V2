package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.UserVoucher;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {

    Optional<UserVoucher> findByUserAndVoucher(Users user, Voucher voucher);
    boolean existsByUserAndVoucher(Users user, Voucher voucher);
    List<UserVoucher> findAllByVoucher(Voucher voucher);
    void deleteByVoucher(Voucher voucher);
}


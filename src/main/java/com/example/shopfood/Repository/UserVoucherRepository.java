package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.UserVoucher;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer>, JpaSpecificationExecutor<UserVoucher> {
}

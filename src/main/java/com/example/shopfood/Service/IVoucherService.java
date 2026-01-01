package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Entity.Voucher;
import com.example.shopfood.Model.Request.Voucher.CreateVoucher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface IVoucherService {
    Voucher createVoucher(CreateVoucher request);
    List<Voucher> getAll();
    void applyVoucher(String code, Order order);
    Optional<Voucher> getVoucherByCode(String code);
    void deleteVoucher(Integer id);
}

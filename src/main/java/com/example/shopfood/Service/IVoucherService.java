package com.example.shopfood.Service;

import com.example.shopfood.Model.DTO.OrderDTO;
import com.example.shopfood.Model.DTO.VoucherApplyResult;
import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Entity.Voucher;
import com.example.shopfood.Model.Request.Voucher.CreateVoucher;
import com.example.shopfood.Model.Request.Voucher.UpdateVoucher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface IVoucherService {

    Voucher createVoucher(CreateVoucher request);

    VoucherApplyResult applyVoucher(
            Integer orderId,
            String code
    );
    List<Voucher> getAllForAdmin();
    List<Voucher> getAllForUsers();


    Optional<Voucher> getVoucherByCode(String code);

    void deleteVoucher(Integer id);

    Voucher updateVoucher(Integer voucherId, UpdateVoucher request);


}


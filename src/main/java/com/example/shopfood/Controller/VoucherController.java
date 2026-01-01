package com.example.shopfood.Controller;

import com.example.shopfood.Model.Entity.Voucher;
import com.example.shopfood.Model.Request.Voucher.CreateVoucher;
import com.example.shopfood.Service.IVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;

    // Tạo mới voucher
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody CreateVoucher request) {
        Voucher voucher = voucherService.createVoucher(request);
        return ResponseEntity.ok(voucher);
    }

    // Lấy tất cả voucher
    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        List<Voucher> vouchers = voucherService.getAll();
        return ResponseEntity.ok(vouchers);
    }

    // Lấy voucher theo mã code
    @GetMapping("/{code}")
    public ResponseEntity<Voucher> getVoucherByCode(@PathVariable String code) {
        return voucherService.getVoucherByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Xoá voucher theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok("Voucher đã được xoá.");
    }
}

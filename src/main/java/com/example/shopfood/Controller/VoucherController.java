package com.example.shopfood.Controller;

import com.example.shopfood.Model.Entity.Voucher;
import com.example.shopfood.Model.Request.Voucher.CreateVoucher;
import com.example.shopfood.Model.Request.Voucher.UpdateVoucher;
import com.example.shopfood.Service.IVoucherService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers")
public class VoucherController {

    @Autowired
    private IVoucherService voucherService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Voucher> createVoucher(@Valid @RequestBody CreateVoucher request) {
        Voucher voucher = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(voucher);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Integer id,
                                                 @Valid @RequestBody UpdateVoucher request) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Voucher>> getAllVouchersForAdmin() {
        return ResponseEntity.ok(voucherService.getAllForAdmin());
    }

    @GetMapping("/user")
    public ResponseEntity<List<Voucher>> getAllVouchersForUsers() {
        return ResponseEntity.ok(voucherService.getAllForUsers());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Voucher> getVoucherByCode(@PathVariable String code) {
        return voucherService.getVoucherByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok("Xóa voucher thành công");
    }
}

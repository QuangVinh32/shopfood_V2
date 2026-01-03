package com.example.shopfood.Controller;

import com.example.shopfood.Model.Entity.Voucher;
import com.example.shopfood.Model.Request.Voucher.CreateVoucher;
import com.example.shopfood.Model.Request.Voucher.UpdateVoucher;
import com.example.shopfood.Service.IVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
@CrossOrigin("*")
public class VoucherController {

    @Autowired
    private IVoucherService voucherService;

    // =========================
    // CREATE VOUCHER
    // =========================
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(
            @Valid @RequestBody CreateVoucher request
    ) {
        Voucher voucher = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(voucher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateVoucher request
    ) {
        Voucher voucher = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(voucher);
    }


    // =========================
    // GET ALL VOUCHERS
    // =========================
    @GetMapping("/admin")
    public ResponseEntity<List<Voucher>> getAllVouchersForAdmin() {
        return ResponseEntity.ok(voucherService.getAllForAdmin());
    }
    @GetMapping("/user")
    public ResponseEntity<List<Voucher>> getAllVouchersForUsers() {
        return ResponseEntity.ok(voucherService.getAllForUsers());
    }

    // =========================
    // GET VOUCHER BY CODE
    // =========================
    @GetMapping("/{code}")
    public ResponseEntity<Voucher> getVoucherByCode(
            @PathVariable String code
    ) {
        return voucherService.getVoucherByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // DELETE VOUCHER
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVoucher(
            @PathVariable Integer id
    ) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok("Xóa voucher thành công");
    }
    // Voucher có thể gỡ khỏi order hay không thì đã add tránh trường hợp
    // add sai và khi nào cần trạng thái thì để đơn hàng thành công trong quá trình vận chuyển k thể update nữa
}


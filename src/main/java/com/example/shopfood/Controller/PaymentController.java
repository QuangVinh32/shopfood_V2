package com.example.shopfood.Controller;

import com.example.shopfood.Service.IMomoPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private IMomoPaymentService momoPaymentService;

    /**
     * User gọi để khởi tạo thanh toán Momo cho 1 đơn.
     * Trả về URL Momo → FE redirect user sang đây.
     */
    @PostMapping("/momo/create")
    public ResponseEntity<?> createMomoPayment(@RequestParam Integer orderId) throws Exception {
        String payUrl = momoPaymentService.createPaymentRequest(orderId);
        return ResponseEntity.ok(Map.of("payUrl", payUrl));
    }

    /**
     * Webhook do Momo server gọi (server-to-server). Public, không cần auth.
     * Bảo mật bằng signature HMAC.
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<?> momoIpn(@RequestBody Map<String, Object> payload) {
        try {
            momoPaymentService.handleIpn(payload);
            return ResponseEntity.ok(Map.of("status", 0, "message", "OK"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("status", 99, "message", "Invalid signature"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", 99, "message", e.getMessage()));
        }
    }
}

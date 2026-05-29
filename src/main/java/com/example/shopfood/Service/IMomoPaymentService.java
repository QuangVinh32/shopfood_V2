package com.example.shopfood.Service;

import java.util.Map;

public interface IMomoPaymentService {

    /**
     * Tạo yêu cầu thanh toán Momo cho 1 order → trả về payUrl để FE redirect.
     */
    String createPaymentRequest(Integer orderId) throws Exception;

    /**
     * Xử lý IPN (server-to-server callback) từ Momo.
     */
    void handleIpn(Map<String, Object> payload) throws Exception;
}

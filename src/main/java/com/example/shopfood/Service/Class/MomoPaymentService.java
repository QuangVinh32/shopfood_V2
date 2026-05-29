package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Entity.OrderStatus;
import com.example.shopfood.Model.Entity.Payment;
import com.example.shopfood.Model.Entity.PaymentStatus;
import com.example.shopfood.Repository.OrderRepository;
import com.example.shopfood.Repository.PaymentRepository;
import com.example.shopfood.Service.IMomoPaymentService;
import com.example.shopfood.Utils.CurrentUserUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MomoPaymentService implements IMomoPaymentService {

    private static final Logger log = LoggerFactory.getLogger(MomoPaymentService.class);

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CurrentUserUtil currentUserUtil;
    @Autowired private ObjectMapper objectMapper;

    @Value("${app.momo.partner-code}") private String partnerCode;
    @Value("${app.momo.access-key}")   private String accessKey;
    @Value("${app.momo.secret-key}")   private String secretKey;
    @Value("${app.momo.endpoint}")     private String endpoint;
    @Value("${app.momo.return-url}")   private String returnUrl;
    @Value("${app.momo.ipn-url}")      private String ipnUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Tạo request thanh toán Momo cho 1 đơn → trả về payUrl để FE redirect user sang Momo.
     */
    @Override
    @Transactional
    public String createPaymentRequest(Integer orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        if (!order.getUser().getUserId().equals(currentUserUtil.currentUser().getUserId())) {
            throw new AccessDeniedException("Không có quyền thanh toán đơn này");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Đơn không ở trạng thái chờ thanh toán");
        }

        String requestId = "REQ-" + order.getOrderId() + "-" + System.currentTimeMillis();
        String orderIdMomo = "ORD-" + order.getOrderId() + "-" + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang Shopfood #" + order.getOrderId();
        long amount = order.getTotalAmount();
        String extraData = "";
        String requestType = "captureWallet";

        // Tạo signature theo doc Momo
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderIdMomo +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSHA256(rawSignature, secretKey);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("partnerName", "Shopfood");
        body.put("storeId", "ShopfoodStore");
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderIdMomo);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", returnUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("lang", "vi");
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Lưu Payment PENDING
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setProvider("MOMO");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount((int) amount);
        payment.setTransactionId(orderIdMomo);
        paymentRepository.save(payment);

        ResponseEntity<String> resp = restTemplate.postForEntity(
                endpoint, new HttpEntity<>(body, headers), String.class);

        JsonNode json = objectMapper.readTree(resp.getBody());
        payment.setGatewayResponse(resp.getBody());
        paymentRepository.save(payment);

        if (json.has("payUrl")) {
            return json.get("payUrl").asText();
        }
        throw new RuntimeException("Momo không trả về payUrl: " + resp.getBody());
    }

    /**
     * Xử lý IPN (Instant Payment Notification) từ Momo.
     * Momo POST tới /api/payments/momo/ipn khi user thanh toán xong.
     */
    @Override
    @Transactional
    public void handleIpn(Map<String, Object> payload) throws Exception {
        // Verify signature
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + payload.get("amount") +
                "&extraData=" + payload.getOrDefault("extraData", "") +
                "&message=" + payload.getOrDefault("message", "") +
                "&orderId=" + payload.get("orderId") +
                "&orderInfo=" + payload.getOrDefault("orderInfo", "") +
                "&orderType=" + payload.getOrDefault("orderType", "") +
                "&partnerCode=" + payload.get("partnerCode") +
                "&payType=" + payload.getOrDefault("payType", "") +
                "&requestId=" + payload.get("requestId") +
                "&responseTime=" + payload.getOrDefault("responseTime", "") +
                "&resultCode=" + payload.get("resultCode") +
                "&transId=" + payload.getOrDefault("transId", "");

        String expected = hmacSHA256(rawSignature, secretKey);
        String received = String.valueOf(payload.get("signature"));
        if (!expected.equalsIgnoreCase(received)) {
            log.warn("Momo IPN signature mismatch for orderId={}", payload.get("orderId"));
            throw new SecurityException("Invalid signature");
        }

        String orderIdMomo = String.valueOf(payload.get("orderId"));
        Payment payment = paymentRepository.findByTransactionId(orderIdMomo)
                .orElseThrow(() -> new RuntimeException("Payment không tồn tại: " + orderIdMomo));

        Integer resultCode = ((Number) payload.get("resultCode")).intValue();
        payment.setGatewayResponse(objectMapper.writeValueAsString(payload));

        if (resultCode == 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(new Date());
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            if (order.getOrderStatus() == OrderStatus.PENDING) {
                order.setOrderStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

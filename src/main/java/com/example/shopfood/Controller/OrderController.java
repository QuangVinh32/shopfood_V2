package com.example.shopfood.Controller;

import com.example.shopfood.Model.DTO.OrderDTO;
import com.example.shopfood.Model.DTO.OrderGetDTO;
import com.example.shopfood.Model.DTO.VoucherApplyResult;
import com.example.shopfood.Model.Entity.PaymentMethod;
import com.example.shopfood.Model.Request.Order.FilterOrder;
import com.example.shopfood.Model.Request.Order.UpdateOrder;
import com.example.shopfood.Service.IOrderService;
import com.example.shopfood.Service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/orders"})
public class OrderController {
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IVoucherService voucherService;


    @PostMapping("/{orderId}/apply-voucher")
    public ResponseEntity<VoucherApplyResult> applyVoucher(
            @PathVariable Integer orderId,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(
                voucherService.applyVoucher(orderId, code)
        );
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<OrderGetDTO>> getAllOrdersForAdmin(
            Pageable pageable,
            FilterOrder filterOrder
    ) {
        return ResponseEntity.ok(
                orderService.getAllOrdersPage(pageable, filterOrder)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<Page<OrderGetDTO>> getMyOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(pageable));
    }

    /**
     * @deprecated dùng {@link #checkout} thay thế. Endpoint này không có shipping address.
     */
    @Deprecated
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestParam(required = false) String voucherCode) {
        try {
            orderService.createOrder(voucherCode);
            return ResponseEntity.ok("Đơn hàng đã được tạo thành công. (Khuyến nghị dùng POST /checkout)");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi tạo đơn hàng.");
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestParam Integer shippingAddressId,
            @RequestParam(required = false) String voucherCode,
            @RequestParam(required = false) String note,
            @RequestParam(required = false, defaultValue = "COD") PaymentMethod paymentMethod
    ) {
        try {
            Integer orderId = orderService.createOrderFull(shippingAddressId, voucherCode, note, paymentMethod);
            return ResponseEntity.ok(Map.of(
                    "orderId", orderId,
                    "paymentMethod", paymentMethod,
                    "nextStep", paymentMethod == PaymentMethod.MOMO
                            ? "Gọi POST /api/payments/momo/create?orderId=" + orderId + " để lấy payUrl"
                            : "Đơn COD đã ghi nhận, thanh toán khi nhận hàng"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi tạo đơn hàng.");
        }
    }

    @PutMapping({"/{orderId}"})
    public ResponseEntity<?> updateOrder(@PathVariable("orderId") int orderId, @RequestBody UpdateOrder updateOrder) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(orderId, updateOrder);
            return ResponseEntity.ok(updatedOrder);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping({"/{id}"})
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable int id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/admin/revenue")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Long>> getRevenue() {
        return ResponseEntity.ok(Map.of(
                "originalRevenue", orderService.getTotalOriginalRevenue(),
                "totalDiscount", orderService.getTotalDiscount(),
                "netRevenue", orderService.getTotalRevenue()
        ));
    }
}

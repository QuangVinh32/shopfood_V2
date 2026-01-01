package com.example.shopfood.Controller;

import com.example.shopfood.Model.DTO.OrderDTO;
import com.example.shopfood.Model.DTO.ProductForAdmin;
import com.example.shopfood.Model.Entity.Order;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Request.Order.FilterOrder;
import com.example.shopfood.Model.Request.Order.UpdateOrder;
import com.example.shopfood.Model.Request.Product.FilterProduct;
import com.example.shopfood.Service.Class.OrderService;
import com.example.shopfood.Service.IOrderService;
import org.apache.catalina.mapper.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/orders"})
public class OrderController {
    @Autowired
    private IOrderService orderService;
    @Autowired
    private ModelMapper mapper;

    @GetMapping({"/get-all"})
    public ResponseEntity<Page<OrderDTO>> findAllOrderPage(Pageable pageable, @ModelAttribute FilterOrder filterOrder) {
        Page<Order> productsPage = orderService.getAllOrdersPage(pageable, filterOrder);
        Page<OrderDTO> orderDTOPage = productsPage.map((order) -> mapper.map(order, OrderDTO.class));
        return ResponseEntity.ok(orderDTOPage);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestParam(required = false) String voucherCode) {
        try {
            orderService.createOrder(voucherCode);
            return ResponseEntity.ok("Đơn hàng đã được tạo thành công.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // <-- xử lý RuntimeException
        } catch (Exception e) {
            e.printStackTrace(); // Log ra console để biết nguyên nhân
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra khi tạo đơn hàng.");
        }
    }

    @PutMapping({"/{orderId}"})
    public ResponseEntity<?> updateOrder(@PathVariable("orderId") int orderId, @RequestBody UpdateOrder updateOrder) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(orderId, updateOrder);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<Void> deleteOrder(@PathVariable int id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable int id) {
        OrderDTO orderDTO = orderService.getOrderById(id);
        return ResponseEntity.ok(orderDTO);
    }
}

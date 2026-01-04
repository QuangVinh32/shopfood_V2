package com.example.shopfood.Controller;

import com.example.shopfood.Service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/carts")
@CrossOrigin("*")
@Validated
public class CartController {

    @Autowired
    private ICartService cartService;

    // Phương thức 1: Thêm sản phẩm với size cụ thể
    @PostMapping("/add/{productId}/{productSizeId}")
    public ResponseEntity<?> addProductToCart(
            @PathVariable Integer productId,
            @PathVariable Integer productSizeId) {
        try {
            cartService.addProductToCart(productId, productSizeId);
            return ResponseEntity.ok("Product added to cart successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Phương thức 2: Giảm số lượng sản phẩm (1 item)
    @PutMapping("/decrease/{productId}/{productSizeId}")
    public ResponseEntity<String> decreaseProductQuantity(
            @PathVariable Integer productId,
            @PathVariable Integer productSizeId) {
        try {
            cartService.removeProductFromCart(productId, productSizeId);
            return ResponseEntity.ok("Product quantity decreased");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Phương thức 3: Xóa hoàn toàn sản phẩm (tất cả số lượng)
    @DeleteMapping("/remove/{productId}/{productSizeId}")
    public ResponseEntity<String> removeProductFromCart(
            @PathVariable Integer productId,
            @PathVariable Integer productSizeId) {
        try {
            cartService.deleteByCartId(productId, productSizeId);
            return ResponseEntity.ok("Product removed from cart");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Phương thức 4: Xóa toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart() {
        try {
            cartService.clearCart();
            return ResponseEntity.ok("Cart has been cleared successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Phương thức 5: Lấy thông tin giỏ hàng
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems() {
        try {
            return ResponseEntity.ok(cartService.getCartDetails());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Phương thức 6: Lấy tổng tiền giỏ hàng
    @GetMapping("/total")
    public ResponseEntity<?> getCartTotal() {
        try {
            return ResponseEntity.ok(cartService.getCartTotal());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Phương thức 7: Cập nhật số lượng sản phẩm (tùy chọn - nếu cần)
    @PutMapping("/update/{productId}/{productSizeId}/{quantity}")
    public ResponseEntity<String> updateProductQuantity(
            @PathVariable Integer productId,
            @PathVariable Integer productSizeId,
            @PathVariable Integer quantity) {
        try {
            // Cần thêm phương thức này trong service
            // cartService.updateProductQuantity(productId, productSizeId, quantity);
            return ResponseEntity.ok("Product quantity updated");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
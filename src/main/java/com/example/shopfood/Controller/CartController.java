package com.example.shopfood.Controller;

import com.example.shopfood.Service.ICartService;
import org.springframework.validation.annotation.Validated;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping({"api/v1/carts"})
@CrossOrigin({"*"})
@Validated
public class CartController {
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ICartService cartService;

    @PostMapping({"/add/{productId}"})
    public ResponseEntity<?> addProductToCart(@PathVariable Integer productId) {
        cartService.addProductToCart(productId);
        return ResponseEntity.ok("Product added to cart");
    }

    @DeleteMapping({"/remove/{productId}"})
    public ResponseEntity<String> removeProductFromCart(@PathVariable Integer productId) {
        try {
            cartService.removeProductFromCart(productId);
            return ResponseEntity.ok("Product delete to cart");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping({"/clear"})
    public ResponseEntity<String> clearCart() {
        try {
            cartService.clearCart();
            return ResponseEntity.ok("Cart has been cleared successfully.");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping({"/delete/{productId}"})
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Integer productId) {
        try {
            cartService.deleteByCartId(productId);
            return ResponseEntity.ok("Product removed from cart successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Repository.*;
import com.example.shopfood.Service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService implements ICartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Override
    public void addProductToCart(Integer productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null || username.equalsIgnoreCase("anonymousUser")) {
            throw new RuntimeException("You must be logged in to add products to cart");
        }

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartDetailPK cartDetailPK = new CartDetailPK(cart.getCartId(), product.getProductId());
        Optional<CartDetail> existingCartDetail = cartDetailRepository.findById(cartDetailPK);

        if (existingCartDetail.isPresent()) {
            CartDetail cartDetail = existingCartDetail.get();
            cartDetail.setQuantity(cartDetail.getQuantity() + 1);
            cartDetailRepository.save(cartDetail);
        } else {
            CartDetail newCartDetail = new CartDetail();
            newCartDetail.setCart(cart);
            newCartDetail.setProduct(product);
            newCartDetail.setQuantity(1);
            cartDetailRepository.save(newCartDetail);
        }

        updateCartTotal(cart);
    }

    @Override
    public void removeProductFromCart(Integer productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartDetailPK cartDetailPK = new CartDetailPK(cart.getCartId(), product.getProductId());
        CartDetail cartDetail = cartDetailRepository.findById(cartDetailPK)
                .orElseThrow(() -> new RuntimeException("CartDetail not found"));

        int newQuantity = cartDetail.getQuantity() - 1;
        if (newQuantity > 0) {
            cartDetail.setQuantity(newQuantity);
            cartDetailRepository.save(cartDetail);
        } else {
            cartDetailRepository.delete(cartDetail);
        }

        List<CartDetail> remainingItems = cartDetailRepository.findByCart(cart);
        if (remainingItems.isEmpty()) {
            cartRepository.delete(cart);
        } else {
            updateCartTotal(cart);
        }
    }

    @Override
    public void deleteByCartId(Integer productId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null || username.equalsIgnoreCase("anonymousUser")) {
            throw new RuntimeException("You must be logged in to delete items from cart");
        }

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartDetailPK pk = new CartDetailPK(cart.getCartId(), product.getProductId());

        if (!cartDetailRepository.existsById(pk)) {
            throw new RuntimeException("Product not found in cart");
        }

        cartDetailRepository.deleteById(pk);
        List<CartDetail> remaining = cartDetailRepository.findByCart(cart);

        if (remaining.isEmpty()) {
            cartRepository.delete(cart);
        } else {
            updateCartTotal(cart);
        }
    }

    @Override
    public void clearCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null || username.equalsIgnoreCase("anonymousUser")) {
            throw new RuntimeException("You must be logged in to clear the cart");
        }

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);
        cartDetailRepository.deleteAll(cartDetails);
        cartRepository.delete(cart);
    }

    private void updateCartTotal(Cart cart) {
        List<CartDetail> cartDetails = cart.getCartDetails();
        if (cartDetails == null || cartDetails.isEmpty()) {
            cartDetails = this.cartDetailRepository.findByCart(cart);
        }

        double cartTotal = cartDetails.stream().mapToDouble((cd) -> cd.getProduct().getPrice() * ((double)(100 - cd.getProduct().getDiscount()) / (double)100.0F) * (double)cd.getQuantity()).sum();
        this.cartRepository.save(cart);
    }
}

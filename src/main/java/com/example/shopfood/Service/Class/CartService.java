package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.*;
import com.example.shopfood.Repository.*;
import com.example.shopfood.Service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService implements ICartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Override
    @Transactional
    public void addProductToCart(Integer productId, Integer productSizeId) {
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        if (fullName == null || fullName.equalsIgnoreCase("anonymousUser")) {
            throw new RuntimeException("You must be logged in to add products to cart");
        }

        Users user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setTotal(0.0);
            cart = cartRepository.save(cart);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductSize productSize = productSizeRepository.findById(productSizeId)
                .orElseThrow(() -> new RuntimeException("Product size not found"));

        if (!productSize.getProduct().getProductId().equals(productId)) {
            throw new RuntimeException("This size does not belong to the selected product");
        }

        if (productSize.getQuantity() <= 0) {
            throw new RuntimeException("This size is out of stock");
        }

        Optional<CartDetail> existingCartDetail = cartDetailRepository
                .findByCartAndProductAndProductSize(cart, product, productSize);

        if (existingCartDetail.isPresent()) {
            CartDetail cartDetail = existingCartDetail.get();
            if (cartDetail.getQuantity() + 1 > productSize.getQuantity()) {
                throw new RuntimeException("Not enough stock for this size");
            }
            cartDetail.setQuantity(cartDetail.getQuantity() + 1);
            cartDetailRepository.save(cartDetail);
        } else {
            CartDetail newCartDetail = new CartDetail();
            newCartDetail.setCart(cart);
            newCartDetail.setProduct(product);
            newCartDetail.setProductSize(productSize);
            newCartDetail.setQuantity(1);
            cartDetailRepository.save(newCartDetail);
        }

        updateCartTotal(cart);
    }

    @Override
    @Transactional
    public void removeProductFromCart(Integer productId, Integer productSizeId) {
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductSize productSize = productSizeRepository.findById(productSizeId)
                .orElseThrow(() -> new RuntimeException("Product size not found"));

        CartDetail cartDetail = cartDetailRepository
                .findByCartAndProductAndProductSize(cart, product, productSize)
                .orElseThrow(() -> new RuntimeException("CartDetail not found"));

        int newQuantity = cartDetail.getQuantity() - 1;
        if (newQuantity > 0) {
            cartDetail.setQuantity(newQuantity);
            cartDetailRepository.save(cartDetail);
        } else {
            cartDetailRepository.delete(cartDetail);
        }

        // LUÔN cập nhật total trước
        updateCartTotal(cart);

        // Kiểm tra và xóa cart nếu rỗng
        List<CartDetail> remainingItems = cartDetailRepository.findByCart(cart);
        if (remainingItems.isEmpty()) {
            cartRepository.delete(cart);
        }
    }

    @Override
    @Transactional
    public void deleteByCartId(Integer productId, Integer productSizeId) {
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        if (fullName == null || fullName.equalsIgnoreCase("anonymousUser")) {
            throw new RuntimeException("You must be logged in to delete items from cart");
        }

        Users user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductSize productSize = productSizeRepository.findById(productSizeId)
                .orElseThrow(() -> new RuntimeException("Product size not found"));

        CartDetail cartDetail = cartDetailRepository
                .findByCartAndProductAndProductSize(cart, product, productSize)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        cartDetailRepository.delete(cartDetail);

        // LUÔN cập nhật total trước khi kiểm tra
        updateCartTotal(cart);

        // Kiểm tra nếu cart rỗng thì xóa
        List<CartDetail> remaining = cartDetailRepository.findByCart(cart);
        if (remaining.isEmpty()) {
            cartRepository.delete(cart);
        }
    }

    @Override
    @Transactional
    public void clearCart() {
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        if (fullName == null || fullName.equalsIgnoreCase("anonymousUser")) {
            throw new RuntimeException("You must be logged in to clear the cart");
        }

        Users user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            return; // Không có cart để clear
        }

        // Reset total về 0 trước
        cart.setTotal(0.0);
        cartRepository.save(cart);

        // Xóa cart details
        List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);
        if (!cartDetails.isEmpty()) {
            cartDetailRepository.deleteAll(cartDetails);
        }

        // Xóa cart
        cartRepository.delete(cart);
    }

    private void updateCartTotal(Cart cart) {
        List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);

        // Nếu không có cart details, set total = 0
        if (cartDetails == null || cartDetails.isEmpty()) {
            cart.setTotal(0.0);
            cartRepository.save(cart);
            return;
        }

        // Tính tổng tiền CHÍNH XÁC
        double cartTotal = cartDetails.stream()
                .mapToDouble(cartDetail -> {
                    ProductSize productSize = cartDetail.getProductSize();
                    if (productSize == null) {
                        return 0.0; // Hoặc throw exception
                    }

                    double price = productSize.getPrice();
                    int discount = productSize.getDiscount() != null ? productSize.getDiscount() : 0;
                    int quantity = cartDetail.getQuantity();

                    // Công thức tính: giá * (100 - discount%) / 100 * số lượng
                    return price * (100 - discount) / 100.0 * quantity;
                })
                .sum();

        // Làm tròn nếu cần (2 chữ số thập phân)
        cartTotal = Math.round(cartTotal * 100.0) / 100.0;

        cart.setTotal(cartTotal);
        cartRepository.save(cart);
    }

    @Override
    public List<CartDetail> getCartDetails() {
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            return List.of(); // Trả về list rỗng
        }

        return cartDetailRepository.findByCart(cart);
    }

    // Thêm phương thức để lấy tổng tiền
    public Double getCartTotal() {
        String fullName = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            return 0.0;
        }

        return cart.getTotal();
    }


}
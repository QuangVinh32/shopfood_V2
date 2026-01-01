package com.example.shopfood.Service;

import org.springframework.stereotype.Service;

@Service
public interface ICartService {

    void addProductToCart(Integer productId);

    void removeProductFromCart(Integer productId);

    void deleteByCartId(Integer productId);

    void clearCart();
}

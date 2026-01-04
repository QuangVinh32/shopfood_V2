package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.CartDetail;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ICartService {

    void addProductToCart(Integer productId, Integer productSizeId); // Thêm productSizeId
    void removeProductFromCart(Integer productId, Integer productSizeId); // Thêm productSizeId
    void deleteByCartId(Integer productId, Integer productSizeId); // Thêm productSizeId
    void clearCart();
    List<CartDetail> getCartDetails();
    Double getCartTotal();;
}

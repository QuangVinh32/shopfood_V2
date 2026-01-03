package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IFavouriteService {
    boolean toggleFavourite(Integer productId);
    List<Product> getMyFavourites();
    boolean isFavourite(Integer productId);
}


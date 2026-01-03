package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Favourite;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Repository.FavouriteRepository;
import com.example.shopfood.Repository.ProductRepository;
import com.example.shopfood.Repository.UserRepository;
import com.example.shopfood.Service.IFavouriteService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FavouriteService implements IFavouriteService {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Users getCurrentUser() {
        String fullName = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByFullName(fullName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean toggleFavourite(Integer productId) {

        Users user = getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Favourite> fav =
                favouriteRepository.findByUserAndProduct(user, product);

        if (fav.isPresent()) {
            favouriteRepository.delete(fav.get());
            return false; // đã bỏ yêu thích
        } else {
            Favourite f = new Favourite();
            f.setUser(user);
            f.setProduct(product);
            favouriteRepository.save(f);
            return true; // đã yêu thích
        }
    }

    @Override
    public List<Product> getMyFavourites() {
        Users user = getCurrentUser();
        return favouriteRepository.findByUser(user)
                .stream()
                .map(Favourite::getProduct)
                .toList();
    }

    @Override
    public boolean isFavourite(Integer productId) {
        Users user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return favouriteRepository.existsByUserAndProduct(user, product);
    }
}


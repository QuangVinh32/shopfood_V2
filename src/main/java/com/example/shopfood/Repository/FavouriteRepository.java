package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Favourite;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository
        extends JpaRepository<Favourite, Integer> {

    Optional<Favourite> findByUserAndProduct(Users user, Product product);

    boolean existsByUserAndProduct(Users user, Product product);

    List<Favourite> findByUser(Users user);

    void deleteByUserAndProduct(Users user, Product product);
}

package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.ShippingAddress;
import com.example.shopfood.Model.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Integer> {
    List<ShippingAddress> findByUserOrderByIsDefaultDescIdDesc(Users user);

    Optional<ShippingAddress> findByIdAndUser(Integer id, Users user);

    Optional<ShippingAddress> findByUserAndIsDefaultTrue(Users user);

    @Modifying
    @Query("UPDATE ShippingAddress s SET s.isDefault = false WHERE s.user = :user")
    void clearDefaultForUser(@Param("user") Users user);
}

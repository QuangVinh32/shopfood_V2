package com.example.shopfood.Model.Entity;

import lombok.Data;

import java.util.List;
import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "users"
)
public class Users {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer userId;
    @Column(
            name = "username",
            nullable = false,
            unique = true,
            updatable = false
    )
    private String username;
    @Column(
            name = "password",
            nullable = false
    )
    private String password;
    @Column(
            name = "phone",
            nullable = false,
            unique = true
    )
    private String phone;
    @Column(
            name = "full_name"
    )
    private String fullName;
    @Column(
            name = "address",
            nullable = false
    )
    private String address;
    @Column(
            name = "email",
            nullable = false,
            unique = true
    )
    private String email;
    @Column(
            name = "image"
    )
    private String image;
    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 8
    )
    private Role role;

    // KHÔNG cascade ALL — không xóa đơn/review khi xóa user (giữ lịch sử)
    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    @OneToMany(mappedBy = "user")
    private List<Review> reviews;

    // Cart có thể xóa cùng user
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;
}

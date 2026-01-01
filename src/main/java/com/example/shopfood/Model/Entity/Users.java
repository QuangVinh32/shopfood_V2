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
            nullable = false,
            unique = true
    )
    private String address;
    @Column(
            name = "email",
            nullable = false
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
    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.ALL}
    )
    private List<Order> orders;
    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.ALL}
    )
    private List<Review> reviews;
    @OneToOne(
            mappedBy = "user",
            cascade = {CascadeType.ALL}
    )
    private Cart cart;
}
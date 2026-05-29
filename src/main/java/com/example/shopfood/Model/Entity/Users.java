package com.example.shopfood.Model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore  // KHÔNG serialize password — defense-in-depth nếu lỡ trả Users entity
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

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verify_token", length = 100)
    private String emailVerifyToken;

    @Column(name = "email_verify_expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date emailVerifyExpiresAt;

    @Column(name = "reset_password_token", length = 100)
    private String resetPasswordToken;

    @Column(name = "reset_password_expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date resetPasswordExpiresAt;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    // KHÔNG cascade ALL — không xóa đơn/review khi xóa user (giữ lịch sử)
    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    @OneToMany(mappedBy = "user")
    private List<Review> reviews;

    // Cart có thể xóa cùng user
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;
}

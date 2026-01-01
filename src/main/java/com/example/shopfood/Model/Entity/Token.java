package com.example.shopfood.Model.Entity;
import lombok.Data;
import java.util.Date;
import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "token"
)
public class Token {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id"
    )
    private Integer id;
    @Column(
            name = "token",
            length = 1000,
            nullable = false
    )
    private String token;
    @Column(
            name = "user_agent"
    )
    private String userAgent;
    @Column(
            name = "expiration",
            nullable = false
    )
    private Date expiration;
}

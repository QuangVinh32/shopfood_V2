package com.example.shopfood.Model.Entity;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "banners"
)
public class Banner {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer bannerId;
    @Column(
            name = "banner_name",
            length = 255
    )
    private String bannerName;
    @Column(
            name = "banner_image",
            length = 500
    )
    private String bannerImage;
    @Column(
            name = "description",
            length = 255
    )
    private String description;
}

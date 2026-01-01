package com.example.shopfood.Model.Entity;
import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "categories"
)
public class Category {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer categoryId;
    @Enumerated(EnumType.STRING)
    @Column(
            name = "category_status",
            nullable = false,
            length = 15
    )
    private CategoryStatus categoryStatus;
    @Column(
            name = "category_image"
    )
    private String categoryImage;
}

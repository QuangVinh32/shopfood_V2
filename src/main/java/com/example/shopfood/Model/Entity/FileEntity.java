package com.example.shopfood.Model.Entity;
import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(
        name = "files"
)
public class FileEntity {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @Column(
            name = "name"
    )
    private String name;
    @Column(
            name = "path"
    )
    private String path;
}
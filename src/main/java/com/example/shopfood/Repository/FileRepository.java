package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByName(String name);

}

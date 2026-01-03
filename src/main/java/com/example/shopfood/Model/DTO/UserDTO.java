package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.Users;
import lombok.Data;

import java.nio.file.Paths;

@Data
public class UserDTO {
    private String fullName;
    private String image;

    public UserDTO(Users user) {
        this.fullName = user.getFullName();
        this.image = user.getImage();
        if (user.getImage() != null){
            String fileName = Paths.get(user.getImage()).getFileName().toString();
            this.image = "http://localhost:8080/files/image/" + fileName;
        }
    }
}



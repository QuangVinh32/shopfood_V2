package com.example.shopfood.Model.DTO;

import com.example.shopfood.Model.Entity.Users;
import lombok.Data;

@Data
public class UserDTO {
    private String fullName;
    private String image;

    public UserDTO(Users user) {
        this.fullName = user.getFullName();
        this.image = user.getImage();
    }
}

package com.example.shopfood.Model.Request.Category;

import com.example.shopfood.Model.Entity.CategoryStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateCategory {
    private CategoryStatus categoryStatus;
    private MultipartFile categoryImage;

}

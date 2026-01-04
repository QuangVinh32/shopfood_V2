package com.example.shopfood.Model.Request.Product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UpdateProduct {
    private String productName;
    private String description;
    private List<MultipartFile> productImages; // update ảnh
    private List<ProductSizeRequest> sizes;     // update size
    private Integer categoryId;


}

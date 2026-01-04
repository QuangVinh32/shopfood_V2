package com.example.shopfood.Model.Request.Product;

import com.example.shopfood.Model.Entity.ProductSizeEnum;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ProductSizeRequest {
    private Integer productSizeId;
    
    @NotNull(message = "Size name is required")
    private ProductSizeEnum sizeName;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be >= 0")
    private Double price;
    
    @Min(value = 0, message = "Discount must be >= 0")
    @Max(value = 100, message = "Discount must be <= 100")
    private Integer discount = 0;
    
    @Min(value = 0, message = "Quantity must be >= 0")
    private Integer quantity = 0;
}
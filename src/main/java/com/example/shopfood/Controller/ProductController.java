package com.example.shopfood.Controller;
import com.example.shopfood.Model.DTO.ProductForAdmin;
import com.example.shopfood.Model.DTO.ProductForUser;
import com.example.shopfood.Model.DTO.ProductSizeDTO;
import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.ProductImage;
import com.example.shopfood.Model.Request.Product.CreateProduct;
import com.example.shopfood.Model.Request.Product.FilterProduct;
import com.example.shopfood.Model.Request.Product.UpdateProduct;
import com.example.shopfood.Service.IProductService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping({"/api/products"})
public class ProductController {
    @Autowired
    private IProductService productService;
    @Autowired
    private ModelMapper mapper;

    @GetMapping("/get-all")
    public ResponseEntity<Page<ProductForAdmin>> findAllProductPage(
            @PageableDefault(size = 10, sort = "productId") Pageable pageable,
            @ModelAttribute FilterProduct filterProduct) {

        // Lấy page sản phẩm từ service
        Page<Product> productsPage = productService.getAllProductsPage(pageable, filterProduct);

        // Map Product -> ProductForAdmin DTO
        Page<ProductForAdmin> productForAdminPage = productsPage.map(product -> {
            ProductForAdmin dto = new ProductForAdmin();
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setDescription(product.getDescription());

            // Category status, check null để tránh lỗi
            if (product.getCategory() != null) {
//                dto.setCategoryId(product.getCategory().getCategoryId()); // Thếu dòng này
                dto.setCategoryStatus(product.getCategory().getCategoryStatus());
            }

            // Map hình ảnh sang URL
            List<String> imageUrls = new ArrayList<>();
            if (product.getProductImages() != null) {
                imageUrls = product.getProductImages().stream()
                        .map(img -> "http://localhost:8080/files/image/" + img.getProductImageName())
                        .toList();
            }
            dto.setProductImages(imageUrls);

            // Nếu muốn map ProductSize vào DTO (nếu tách size)
            if (product.getSizes() != null) {
                List<ProductSizeDTO> sizes = product.getSizes().stream()
                        .map(size -> new ProductSizeDTO(
                                size.getSizeName().name(),
                                size.getPrice(),
                                size.getDiscount(),
                                size.getQuantity()
                        ))
                        .toList();
                dto.setSizes(sizes);
            }

            return dto;
        });

        return ResponseEntity.ok(productForAdminPage);
    }




    @PostMapping
    public ResponseEntity<String> createProduct(@ModelAttribute CreateProduct createProduct) {
        try {
            productService.createProduct(createProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body("Product added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred while adding the product: " + e.getMessage());
        }
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<String> updateProduct(@PathVariable int id, @ModelAttribute UpdateProduct updateProduct) {
        try {
            productService.updateProduct(id, updateProduct);
            return ResponseEntity.ok("Product updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception var5) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating product");
        }
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception var3) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found for deletion");
        }
    }

    @GetMapping({"admin/{id}"})
    public ResponseEntity<ProductForAdmin> findProductByIdForAdmin(@PathVariable int id) {
        ProductForAdmin productDTO = productService.getProductByIdForAdmin(id);
        return productDTO != null ? ResponseEntity.ok(productDTO) : ResponseEntity.status(HttpStatus.NOT_FOUND).body((ProductForAdmin) null);
    }

    @GetMapping({"/user/{id}"})
    public ResponseEntity<ProductForUser> findProductByIdForUser(@PathVariable int id) {
//        System.out.println("Fetching product with ID: " + id);
        ProductForUser productDTOv2 = productService.getProductByIdForUser(id);
        if (productDTOv2 != null) {
            return ResponseEntity.ok(productDTOv2);
        } else {
            System.out.println("Product with ID " + id + " not found");
            return ResponseEntity.notFound().build();
        }
    }


}

package com.example.shopfood.Controller;

import com.example.shopfood.Service.Class.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    @PostMapping("/{productId}/images")
    public ResponseEntity<String> uploadImage(
            @PathVariable int productId,
            @RequestParam("image") MultipartFile image) throws IOException {
        productImageService.addImageToProduct(productId, image);
        return ResponseEntity.ok("Image uploaded successfully!");
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<String> deleteImage(
            @PathVariable int productId,
            @PathVariable int imageId) {
        productImageService.deleteImageFromProduct(productId, imageId);
        return ResponseEntity.ok("Image deleted successfully!");
    }
}

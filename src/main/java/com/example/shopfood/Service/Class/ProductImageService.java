package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Product;
import com.example.shopfood.Model.Entity.ProductImage;
import com.example.shopfood.Repository.ProductImageRepository;
import com.example.shopfood.Repository.ProductRepository;
import com.example.shopfood.Service.IProductImageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class ProductImageService implements IProductImageService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private FileService fileService;

    @Override
    public void addImageToProduct(int productId, MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        String imagePath = fileService.uploadImage(image);
        String imageName = new File(imagePath).getName();
        // Tạo ProductImage
        ProductImage productImage = new ProductImage();
        productImage.setProductImageName(imageName);
        productImage.setProductImagePath(imagePath);
        productImage.setProduct(product);

        // Lưu vào DB
        productImageRepository.save(productImage);
    }

    @Override
    public void deleteImageFromProduct(int productId, int imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + imageId));

        if (image.getProduct().getProductId() != productId) {
            throw new IllegalArgumentException("Image does not belong to the specified product");
        }

        // Xóa file vật lý trên ổ cứng (nếu cần)
//        File file = new File(image.getProductImagePath());
//        if (file.exists()) {
//            file.delete();
//        }

        // Xóa bản ghi khỏi DB
        productImageRepository.delete(image);
    }
}

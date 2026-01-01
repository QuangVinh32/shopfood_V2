package com.example.shopfood.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface IProductImageService {
    void addImageToProduct(int productId, MultipartFile image) throws IOException;
    void deleteImageFromProduct(int productId, int imageId);
}

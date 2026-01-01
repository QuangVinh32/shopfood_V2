package com.example.shopfood.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
public interface IFileService {
    String uploadImage(MultipartFile image) throws IOException;
}

package com.example.shopfood.Model.Request.Banner;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateBanner {
    private String bannerName;
    private MultipartFile bannerImage;
    private String description;
}

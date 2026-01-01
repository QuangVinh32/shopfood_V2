package com.example.shopfood.Service.Class;
import com.example.shopfood.Model.Entity.Banner;
import com.example.shopfood.Model.Request.Banner.CreateBanner;
import com.example.shopfood.Model.Request.Banner.UpdateBanner;
import com.example.shopfood.Repository.BannerRepository;
import com.example.shopfood.Service.IBannerService;
import com.example.shopfood.Service.IFileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class BannerService implements IBannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private IFileService fileService;

    public Page<Banner> getAllBanners(Pageable pageable) {
        return bannerRepository.findAll(pageable);
    }

    public Optional<Banner> getBannerById(Integer id) {
        return bannerRepository.findById(id);
    }

    public void createBanner(CreateBanner request) throws IOException {
        String fileName = fileService.uploadImage(request.getBannerImage());
        Banner banner = new Banner();
        banner.setBannerName(request.getBannerName());
        banner.setBannerImage(fileName);
        banner.setDescription(request.getDescription());
        bannerRepository.save(banner);
    }

    public Banner updateBanner(Integer bannerId, UpdateBanner request) throws IOException {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new EntityNotFoundException("Banner not found with id: " + bannerId));

        String bannerName = request.getBannerName();
        MultipartFile bannerImage = request.getBannerImage();
        String description = request.getDescription();

        if (bannerName != null && !bannerName.trim().isEmpty()) {
            banner.setBannerName(bannerName);
        }

        if (bannerImage != null && !bannerImage.isEmpty()) {
            String imagePath = fileService.uploadImage(bannerImage);
            banner.setBannerImage(imagePath);
        }

        if (description != null) {
            banner.setDescription(description);
        }

        return bannerRepository.save(banner);
    }

    public void deleteBanner(Integer id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Banner not found with id: " + id));
        bannerRepository.delete(banner);
    }
}

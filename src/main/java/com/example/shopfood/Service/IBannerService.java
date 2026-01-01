package com.example.shopfood.Service;


import com.example.shopfood.Model.Entity.Banner;
import com.example.shopfood.Model.Request.Banner.CreateBanner;
import com.example.shopfood.Model.Request.Banner.UpdateBanner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Optional;
@Service
public interface IBannerService {
    Banner updateBanner(Integer bannerId, UpdateBanner updateBanner) throws IOException;

    void deleteBanner(Integer bannerId);

    Optional<Banner> getBannerById(Integer bannerId);

    Page<Banner> getAllBanners(Pageable pageable);

    void createBanner(CreateBanner request) throws IOException;
}

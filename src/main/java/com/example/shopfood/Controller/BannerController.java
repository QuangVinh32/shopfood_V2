package com.example.shopfood.Controller;
import com.example.shopfood.Model.DTO.BannerDTO;
import com.example.shopfood.Model.Entity.Banner;
import com.example.shopfood.Model.Request.Banner.CreateBanner;
import com.example.shopfood.Model.Request.Banner.UpdateBanner;
import com.example.shopfood.Service.IBannerService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/banners"})
public class BannerController {
    @Autowired
    private IBannerService bannerService;
    @Autowired
    private ModelMapper mapper;

    @GetMapping
    public ResponseEntity<Page<BannerDTO>> getAllBanners(Pageable pageable) {
        Page<Banner> banners = bannerService.getAllBanners(pageable);
        Page<BannerDTO> bannerDTOs = banners.map((banner) -> mapper.map(banner, BannerDTO.class));
        return ResponseEntity.ok(bannerDTOs);
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<BannerDTO> getBannerById(@PathVariable Integer id) {
        return bannerService.getBannerById(id).map((banner) -> ResponseEntity.ok(mapper.map(banner, BannerDTO.class))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createBanner(@ModelAttribute CreateBanner request) {
        try {
            bannerService.createBanner(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Banner added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An error occurred while adding the product: " + e.getMessage());
        }
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<String> updateBanner(@PathVariable Integer id, @ModelAttribute UpdateBanner request) {
        try {
            bannerService.updateBanner(id, request);
            return ResponseEntity.ok("Banner updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception var5) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong while updating banner");
        }
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<String> deleteBanner(@PathVariable Integer id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok("Banner deleted successfully");
    }
}

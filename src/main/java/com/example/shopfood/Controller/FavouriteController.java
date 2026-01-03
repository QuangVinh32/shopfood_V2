package com.example.shopfood.Controller;

import com.example.shopfood.Service.IFavouriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favourites")
@CrossOrigin("*")
public class FavouriteController {

    @Autowired
    private IFavouriteService favouriteService;

    // Toggle
    @PostMapping("/{productId}")
    public ResponseEntity<Boolean> toggleFavourite(
            @PathVariable Integer productId) {
        return ResponseEntity.ok(
                favouriteService.toggleFavourite(productId)
        );
    }

    // Danh sách yêu thích
    @GetMapping
    public ResponseEntity<?> getMyFavourites() {
        return ResponseEntity.ok(
                favouriteService.getMyFavourites()
        );
    }

    // Check favourite
    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> isFavourite(
            @PathVariable Integer productId) {
        return ResponseEntity.ok(
                favouriteService.isFavourite(productId)
        );
    }
}

package com.example.shopfood.Controller;

import com.example.shopfood.Model.Entity.ShippingAddress;
import com.example.shopfood.Model.Request.Shipping.ShippingAddressRequest;
import com.example.shopfood.Service.IShippingAddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-addresses")
public class ShippingAddressController {

    @Autowired
    private IShippingAddressService service;

    @GetMapping
    public ResponseEntity<List<ShippingAddress>> list() {
        return ResponseEntity.ok(service.listMine());
    }

    @PostMapping
    public ResponseEntity<ShippingAddress> create(@RequestBody @Valid ShippingAddressRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingAddress> update(@PathVariable Integer id,
                                                  @RequestBody @Valid ShippingAddressRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

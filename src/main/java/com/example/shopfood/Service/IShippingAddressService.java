package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.ShippingAddress;
import com.example.shopfood.Model.Request.Shipping.ShippingAddressRequest;

import java.util.List;

public interface IShippingAddressService {

    List<ShippingAddress> listMine();

    ShippingAddress create(ShippingAddressRequest req);

    ShippingAddress update(Integer id, ShippingAddressRequest req);

    void delete(Integer id);

    ShippingAddress getOwnedById(Integer id);
}

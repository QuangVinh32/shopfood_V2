package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.ShippingAddress;
import com.example.shopfood.Model.Entity.Users;
import com.example.shopfood.Model.Request.Shipping.ShippingAddressRequest;
import com.example.shopfood.Repository.ShippingAddressRepository;
import com.example.shopfood.Service.IShippingAddressService;
import com.example.shopfood.Utils.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShippingAddressService implements IShippingAddressService {

    @Autowired private ShippingAddressRepository repository;
    @Autowired private CurrentUserUtil currentUserUtil;

    @Override
    public List<ShippingAddress> listMine() {
        Users me = currentUserUtil.currentUser();
        return repository.findByUserOrderByIsDefaultDescIdDesc(me);
    }

    @Override
    @Transactional
    public ShippingAddress create(ShippingAddressRequest req) {
        Users me = currentUserUtil.currentUser();
        if (req.isDefault()) {
            repository.clearDefaultForUser(me);
        }
        ShippingAddress addr = new ShippingAddress();
        addr.setUser(me);
        copy(addr, req);
        return repository.save(addr);
    }

    @Override
    @Transactional
    public ShippingAddress update(Integer id, ShippingAddressRequest req) {
        Users me = currentUserUtil.currentUser();
        ShippingAddress addr = repository.findByIdAndUser(id, me)
                .orElseThrow(() -> new AccessDeniedException("Địa chỉ không tồn tại hoặc không thuộc về bạn"));
        if (req.isDefault()) {
            repository.clearDefaultForUser(me);
        }
        copy(addr, req);
        return repository.save(addr);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Users me = currentUserUtil.currentUser();
        ShippingAddress addr = repository.findByIdAndUser(id, me)
                .orElseThrow(() -> new AccessDeniedException("Địa chỉ không tồn tại hoặc không thuộc về bạn"));
        repository.delete(addr);
    }

    @Override
    public ShippingAddress getOwnedById(Integer id) {
        Users me = currentUserUtil.currentUser();
        return repository.findByIdAndUser(id, me)
                .orElseThrow(() -> new AccessDeniedException("Địa chỉ không tồn tại hoặc không thuộc về bạn"));
    }

    private void copy(ShippingAddress addr, ShippingAddressRequest req) {
        addr.setReceiverName(req.getReceiverName());
        addr.setReceiverPhone(req.getReceiverPhone());
        addr.setAddressLine(req.getAddressLine());
        addr.setWard(req.getWard());
        addr.setDistrict(req.getDistrict());
        addr.setProvince(req.getProvince());
        addr.setDefault(req.isDefault());
    }
}

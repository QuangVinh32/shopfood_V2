package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer>, JpaSpecificationExecutor<Banner> {
}
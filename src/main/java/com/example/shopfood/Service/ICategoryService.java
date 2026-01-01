package com.example.shopfood.Service;

import com.example.shopfood.Model.Entity.Category;
import com.example.shopfood.Model.Request.Category.CreateCategory;
import com.example.shopfood.Model.Request.Category.UpdateCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Optional;
@Service
public interface ICategoryService {

    Page<Category> getAllCategoryPage(Pageable pageable);

    Optional<Category> findByCategoryId(int categoryId);

    Category updateCategory(int categoryId, UpdateCategory request) throws IOException;

    void createCategory(CreateCategory request) throws IOException;

    void deleteByCategoryId(int categoryId);
}

package com.example.shopfood.Service.Class;

import com.example.shopfood.Model.Entity.Category;
import com.example.shopfood.Model.Request.Category.CreateCategory;
import com.example.shopfood.Model.Request.Category.UpdateCategory;
import com.example.shopfood.Repository.CategoryRepository;
import com.example.shopfood.Service.ICategoryService;
import com.example.shopfood.Service.IFileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Optional;

@Service
public class CategoryService implements ICategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private IFileService fileService;

    public Page<Category> getAllCategoryPage(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Optional<Category> findByCategoryId(int categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Category updateCategory(int categoryId, UpdateCategory request) throws IOException {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        if (request.getCategoryStatus() != null) {
            category.setCategoryStatus(request.getCategoryStatus());
        }

        if (request.getCategoryImage() != null && !request.getCategoryImage().isEmpty()) {
            String imagePath = fileService.uploadImage(request.getCategoryImage());
            category.setCategoryImage(imagePath);
        }

        return categoryRepository.save(category);
    }

    public void createCategory(CreateCategory request) throws IOException {
        String fileName = fileService.uploadImage(request.getCategoryImage());
        Category category = new Category();
        category.setCategoryStatus(request.getCategoryStatus());
        category.setCategoryImage(fileName);
        categoryRepository.save(category);
    }


    public void deleteByCategoryId(int categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        categoryRepository.delete(category);
    }
}

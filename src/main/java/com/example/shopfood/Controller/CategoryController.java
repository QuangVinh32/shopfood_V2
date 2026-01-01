package com.example.shopfood.Controller;

import java.io.IOException;

import com.example.shopfood.Model.DTO.CategoryDTO;
import com.example.shopfood.Model.Entity.Category;
import com.example.shopfood.Model.Request.Category.CreateCategory;
import com.example.shopfood.Model.Request.Category.UpdateCategory;
import com.example.shopfood.Service.ICategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/categories"})
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private ModelMapper mapper;

    @GetMapping({"/get-all"})
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryService.getAllCategoryPage(pageable);
        Page<CategoryDTO> categoryDTOs = categories.map((category) -> mapper.map(category, CategoryDTO.class));
        return ResponseEntity.ok(categoryDTOs);
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable int id) {
        return categoryService.findByCategoryId(id).map((category) -> ResponseEntity.ok(mapper.map(category, CategoryDTO.class))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@ModelAttribute CreateCategory request) throws IOException {
        try {
            categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Category added successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception var4) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong while updating category");
        }
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<?> updateCategory(@PathVariable int id, @ModelAttribute UpdateCategory request) {
        try {
            categoryService.updateCategory(id, request);
            return ResponseEntity.ok("Category updated successfully");
        } catch (Exception var4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found for update");
        }
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<String> deleteCategory(@PathVariable int id) {
        try {
            categoryService.deleteByCategoryId(id);
            return ResponseEntity.ok("Category deleted successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
}


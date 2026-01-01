package com.example.shopfood.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.example.shopfood.Service.IFileService;
import com.example.shopfood.Utils.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin({"*"})
@RestController
@RequestMapping({"/files"})
@Validated
public class FileController {
    @Autowired
    private IFileService fileService;

    @PostMapping({"/image"})
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
        FileManager fileManager = new FileManager();
        if (!fileManager.isTypeFileImage(image)) {
            return new ResponseEntity<>("File must be an image!", HttpStatus.UNPROCESSABLE_ENTITY);
        } else {
            String savedFileName = this.fileService.uploadImage(image);
            return new ResponseEntity<>(savedFileName, HttpStatus.OK);
        }
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) throws IOException {
        String UPLOAD_DIR = "C:\\Users\\ADMIN\\IdeaProjects\\shopfood\\shopfood\\uploads\\images";
        Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }
}
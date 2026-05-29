package com.example.shopfood.Service.Class;
import com.example.shopfood.Model.Entity.FileEntity;
import com.example.shopfood.Repository.FileRepository;
import com.example.shopfood.Service.IFileService;
import com.example.shopfood.Utils.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService implements IFileService {
    @Autowired
    private FileRepository fileRepository;
    private final FileManager fileManager = new FileManager();

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public String uploadImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }

        // Validate magic bytes (không tin Content-Type)
        if (!fileManager.isTypeFileImage(image)) {
            throw new IOException("File không phải là ảnh hợp lệ");
        }

        String originalFileName = image.getOriginalFilename();
        String ext = fileManager.getFormatFile(originalFileName);
        if (ext.isEmpty()) {
            throw new IOException("Định dạng ảnh không được hỗ trợ");
        }

        // UUID filename → chống path traversal + chống collision
        String safeName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        File directory = baseDir.toFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Không tạo được thư mục upload: " + baseDir);
        }

        Path target = baseDir.resolve(safeName).normalize();
        // Defense-in-depth: target phải nằm trong baseDir
        if (!target.startsWith(baseDir)) {
            throw new IOException("Đường dẫn upload không hợp lệ");
        }

        fileManager.createNewMultiPartFile(target.toString(), image);

        FileEntity newFile = new FileEntity();
        newFile.setName(safeName);
        newFile.setPath(target.toString());
        fileRepository.save(newFile);

        return target.toString();
    }
}

package com.example.shopfood.Service.Class;
import com.example.shopfood.Model.Entity.FileEntity;
import com.example.shopfood.Repository.FileRepository;
import com.example.shopfood.Service.IFileService;
import com.example.shopfood.Utils.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
;
@Service
public class FileService implements IFileService {
    @Autowired
    private FileRepository fileRepository;
    private final FileManager fileManager = new FileManager();

    @Override
    public String uploadImage(MultipartFile image) throws IOException {

        // ✅ 1. Ảnh null hoặc rỗng → bỏ qua
        if (image == null || image.isEmpty()) {
            return null;
        }

        String originalFileName = image.getOriginalFilename();

        // phòng trường hợp filename null
        if (originalFileName == null || originalFileName.isBlank()) {
            return null;
        }

        String UPLOAD_DIR = "D:\\Java Sping Boot\\shopfood_V2\\uploads\\images";
        String path = UPLOAD_DIR + File.separator + originalFileName;

        FileEntity existing = fileRepository.findByName(originalFileName).orElse(null);
        if (existing != null && new File(existing.getPath()).exists()) {
            return existing.getPath();
        }

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        fileManager.createNewMultiPartFile(path, image);

        FileEntity newFile = new FileEntity();
        newFile.setName(originalFileName);
        newFile.setPath(path);
//        fileRepository.save(newFile);

        return path;
    }



}

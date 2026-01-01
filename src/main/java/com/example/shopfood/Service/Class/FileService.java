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

    public String uploadImage(MultipartFile image) throws IOException {
        String originalFileName = image.getOriginalFilename();
        String format = fileManager.getFormatFile(originalFileName);
        String UPLOAD_DIR = "C:\\Users\\ADMIN\\IdeaProjects\\shopfood\\shopfood\\uploads\\images";
        String path = UPLOAD_DIR + "\\" + originalFileName;

        // Kiểm tra trong database xem có file trùng tên chưa
        FileEntity existing = fileRepository.findByName(originalFileName).orElse(null);
        if (existing != null && new File(existing.getPath()).exists()) {
            // Nếu file đã tồn tại => dùng lại path cũ
            return existing.getPath();
        }

        // Nếu chưa có, thì upload mới
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

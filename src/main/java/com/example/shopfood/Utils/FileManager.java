package com.example.shopfood.Utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class FileManager {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    public boolean isFileOrFolderExist(String path) {
        return (new File(path)).exists();
    }

    /**
     * Kiểm tra file thực sự là ảnh bằng magic bytes (không tin Content-Type vì spoof được).
     */
    public boolean isTypeFileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 4) return false;
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) return true;
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            if ((header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') return true;
            // GIF: 47 49 46 38
            if (header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8') return true;
            // WEBP: RIFF????WEBP
            if (read >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') return true;
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public void createNewMultiPartFile(String path, MultipartFile multipartFile) throws IllegalStateException, IOException {
        File file = new File(path);
        multipartFile.transferTo(file);
    }

    public String getFormatFile(String input) {
        if (input == null) return "";
        int dot = input.lastIndexOf('.');
        if (dot < 0) return "";
        String ext = input.substring(dot + 1).toLowerCase();
        return ALLOWED_EXT.contains(ext) ? ext : "";
    }
}

package com.studentprofiling.studentprofiling.ServiceClasses;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads/profile-pictures"); // relative to app working dir

    public String saveBase64Image(String base64, String suggestedName) throws IOException {
        if (base64 == null || base64.isBlank()) return null;

        Files.createDirectories(root);

        
        String fileName = (suggestedName != null && !suggestedName.isBlank())
                ? suggestedName + ".jpg"
                : UUID.randomUUID().toString() + ".jpg";

        byte[] bytes = Base64.getDecoder().decode(base64);
        Path out = root.resolve(fileName);
        Files.write(out, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        
        return "/studentprofiling/files/profile-pictures/" + fileName;
    }

    public String saveBase64ImageTo(String subfolder, String baseName, String base64) throws IOException {
    if (base64 == null || base64.isBlank()) return null;
    Path dir = Paths.get("uploads").resolve(subfolder);
    Files.createDirectories(dir);
    String fileName = (baseName != null && !baseName.isBlank() ? baseName : UUID.randomUUID().toString()) + ".jpg";
    byte[] bytes = Base64.getDecoder().decode(base64);
    Path out = dir.resolve(fileName);
    Files.write(out, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    return "/uploads/" + subfolder + "/" + fileName;
}
    
}

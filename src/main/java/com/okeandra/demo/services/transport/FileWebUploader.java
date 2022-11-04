package com.okeandra.demo.services.transport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileWebUploader {
    public boolean singleFileUpload(MultipartFile file, String uploadedFolder, String newFileName) {

        if (file.isEmpty()) {
            return false;
        }

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(uploadedFolder + newFileName);
            Files.write(path, bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}

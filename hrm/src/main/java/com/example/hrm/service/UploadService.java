package com.example.hrm.service;

import jakarta.servlet.ServletContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class UploadService {
    public static String UPLOAD_DIRECTORY = "src/main/resources/static/images/";
    public String handleSaveUploadFile(MultipartFile file, String targetFolder) throws IOException {
        String fileNames ="";
        fileNames=System.currentTimeMillis()+"-"+file.getOriginalFilename();
        Path dirPath = Paths.get(UPLOAD_DIRECTORY + targetFolder);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY+targetFolder+File.separator, fileNames);
        Files.write(fileNameAndPath, file.getBytes());
        return fileNames;
    }

}

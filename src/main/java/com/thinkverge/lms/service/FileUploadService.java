package com.thinkverge.lms.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {

            Map upload = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("resource_type", "auto")
            );

            return upload.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("File upload failed");
        }
    }
}
package com.thinkverge.lms.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            boolean isPdf = "application/pdf".equals(contentType);

            // Build upload options
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", isPdf ? "raw" : "image");

            if (isPdf) {
                // ✅ Force .pdf extension in the Cloudinary public_id
                // This makes the URL end with .pdf so browsers handle it correctly
                String originalName = file.getOriginalFilename();
                String baseName = originalName != null
                    ? originalName.replaceAll("[^a-zA-Z0-9_-]", "_").replaceAll("_pdf$", "")
                    : "document";
                options.put("public_id", baseName + "_" + System.currentTimeMillis() + ".pdf");
            }

            Map upload = cloudinary.uploader().upload(file.getBytes(), options);
            return upload.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }
}
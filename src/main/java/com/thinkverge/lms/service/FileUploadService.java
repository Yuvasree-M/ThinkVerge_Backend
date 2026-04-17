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
            if (contentType == null) contentType = "";

            Map<String, Object> options = new HashMap<>();

            if (contentType.startsWith("video/")) {
                // ── VIDEO ──────────────────────────────────────────
                options.put("resource_type", "video");
                options.put("folder", "lessons/videos");

            } else if ("application/pdf".equals(contentType)) {
                // ── PDF ────────────────────────────────────────────
                options.put("resource_type", "raw");
                options.put("folder", "lessons/pdfs");

                String originalName = file.getOriginalFilename();
                String baseName = originalName != null
                    ? originalName.replaceAll("[^a-zA-Z0-9_-]", "_")
                                  .replaceAll("_pdf$", "")
                    : "document";
                // Force .pdf extension so browsers open it correctly
                options.put("public_id", "lessons/pdfs/" + baseName
                    + "_" + System.currentTimeMillis() + ".pdf");

            } else if (contentType.startsWith("image/")) {
                // ── IMAGE ──────────────────────────────────────────
                options.put("resource_type", "image");
                options.put("folder", "lessons/images");

            } else {
                // ── UNKNOWN — store as raw ─────────────────────────
                options.put("resource_type", "raw");
                options.put("folder", "lessons/files");
            }

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            return result.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }
}
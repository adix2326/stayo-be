package com.stayo.stayo.storage.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.stayo.stayo.storage.dto.StoredFile;
import com.stayo.stayo.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public StoredFile upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto"
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            return new StoredFile(url, publicId);
        } catch (IOException e) {
            log.error("Cloudinary upload failed for folder: {}", folder, e);
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.warn("Failed to delete Cloudinary asset: {}", publicId, e);
        }
    }
}

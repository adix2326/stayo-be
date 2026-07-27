package com.stayo.stayo.storage.service;

import com.stayo.stayo.storage.dto.StoredFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Uploads a file to cloud storage under the given logical folder
     * (e.g. "profile-images", "documents", "property-images").
     * Throws IllegalArgumentException if file is null/empty.
     */
    StoredFile upload(MultipartFile file, String folder);

    /**
     * Deletes a previously uploaded asset by its Cloudinary public_id.
     * No-op if publicId is null/blank.
     */
    void delete(String publicId);
}

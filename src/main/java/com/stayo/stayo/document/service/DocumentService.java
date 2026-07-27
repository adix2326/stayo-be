package com.stayo.stayo.document.service;

import com.stayo.stayo.document.dto.DocumentResponseDTO;
import com.stayo.stayo.document.enums.DocType;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    /**
     * Stores the file on disk (same local-disk `uploads/` pattern used across
     * this backend — see AI_BE_CONTEXT.md §12 on ephemeral local storage) and
     * persists a Document record tagging it with docType.
     */
    DocumentResponseDTO uploadDocument(String userId, DocType docType, MultipartFile file);

    List<DocumentResponseDTO> listForUser(String userId);
}

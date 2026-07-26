package com.stayo.stayo.document.service.impl;

import com.stayo.stayo.document.dto.DocumentResponseDTO;
import com.stayo.stayo.document.entity.Document;
import com.stayo.stayo.document.enums.DocType;
import com.stayo.stayo.document.repository.DocumentRepository;
import com.stayo.stayo.document.service.DocumentService;
import com.stayo.stayo.storage.dto.StoredFile;
import com.stayo.stayo.storage.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    @Override
    public DocumentResponseDTO uploadDocument(String userId, DocType docType, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        StoredFile stored = fileStorageService.upload(file, "documents");

        LocalDateTime now = LocalDateTime.now();
        Document document = Document.builder()
                .fileId(stored.publicId())
                .userId(userId)
                .contentType(file.getContentType())
                .docType(docType)
                .fileUrl(stored.url())
                .isVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Document saved = documentRepository.save(document);
        log.info("Document uploaded for user: {} (docType={})", userId, docType);
        return mapToResponse(saved);
    }

    @Override
    public List<DocumentResponseDTO> listForUser(String userId) {
        return documentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DocumentResponseDTO mapToResponse(Document document) {
        return DocumentResponseDTO.builder()
                .id(document.getId())
                .docType(document.getDocType())
                .fileUrl(document.getFileUrl())
                .isVerified(document.getIsVerified())
                .createdAt(document.getCreatedAt())
                .build();
    }
}

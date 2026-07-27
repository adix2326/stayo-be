package com.stayo.stayo.document.service.impl;

import com.stayo.stayo.document.dto.DocumentResponseDTO;
import com.stayo.stayo.document.entity.Document;
import com.stayo.stayo.document.enums.DocType;
import com.stayo.stayo.document.repository.DocumentRepository;
import com.stayo.stayo.storage.dto.StoredFile;
import com.stayo.stayo.storage.service.FileStorageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link DocumentServiceImpl}.
 * Mocks {@link FileStorageService} to keep tests hermetic (no real Cloudinary calls).
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks private DocumentServiceImpl documentService;

    private static final String USER_ID = "user123";

    private MultipartFile validFile() {
        return new MockMultipartFile("file", "aadhaar.jpg", "image/jpeg", "content".getBytes());
    }

    @Test
    void uploadDocument_success() {
        StoredFile stored = new StoredFile(
                "https://res.cloudinary.com/demo/image/upload/v1/documents/abc.jpg", "documents/abc");
        when(fileStorageService.upload(any(MultipartFile.class), eq("documents"))).thenReturn(stored);
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentResponseDTO response = documentService.uploadDocument(USER_ID, DocType.AADHAR, validFile());

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        Document saved = captor.getValue();

        assertEquals("documents/abc", saved.getFileId());
        assertEquals(stored.url(), saved.getFileUrl());
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(DocType.AADHAR, saved.getDocType());
        assertFalse(saved.getIsVerified());
        assertEquals(stored.url(), response.getFileUrl());
    }

    @Test
    void uploadDocument_emptyFile_throwsIllegalArgumentException() {
        MultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () ->
                documentService.uploadDocument(USER_ID, DocType.PAN, empty));
        verifyNoInteractions(fileStorageService);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void listForUser_mapsAllDocuments() {
        Document doc1 = Document.builder().id("d1").docType(DocType.AADHAR).fileUrl("url1").isVerified(false).build();
        Document doc2 = Document.builder().id("d2").docType(DocType.PAN).fileUrl("url2").isVerified(true).build();
        when(documentRepository.findByUserId(USER_ID)).thenReturn(List.of(doc1, doc2));

        List<DocumentResponseDTO> result = documentService.listForUser(USER_ID);

        assertEquals(2, result.size());
        assertEquals("d1", result.get(0).getId());
        assertEquals("d2", result.get(1).getId());
    }
}

package com.stayo.stayo.document.entity;

import com.stayo.stayo.document.enums.DocType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    private String id;

    private String fileId;

    @Indexed
    private String userId; // reference to users collection

    private String contentType;

    private DocType docType;

    private String fileUrl;

    @Builder.Default
    private Boolean isVerified = false;

    private String verifiedBy; // reference to users collection (role: ADMIN)
    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

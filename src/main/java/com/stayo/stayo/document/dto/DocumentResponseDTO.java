package com.stayo.stayo.document.dto;

import com.stayo.stayo.document.enums.DocType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {
    private String id;
    private DocType docType;
    private String fileUrl;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    // verifiedBy is intentionally omitted — the uploader doesn't need to know which admin reviewed it.
}

package com.stayo.stayo.property.service;

import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.dto.PGResponse;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.PageResponse;





import java.util.List;

public interface PGService {
    List<PGResponse> getFeaturedProperties();
    PageResponse<PGCardDTO> searchPGs(String userId, SearchRequest request);
}

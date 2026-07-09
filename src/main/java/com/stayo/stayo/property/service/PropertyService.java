package com.stayo.stayo.property.service;

import com.stayo.stayo.property.dto.PropertyCardDTO;
import com.stayo.stayo.property.dto.PropertyResponse;
import com.stayo.stayo.search.dto.SearchRequest;
import com.stayo.stayo.shared.dto.PageResponse;




import java.util.List;

public interface PropertyService {
    List<PropertyResponse> getFeaturedProperties();
    PageResponse<PropertyCardDTO> searchProperties(String userId, SearchRequest request);
}

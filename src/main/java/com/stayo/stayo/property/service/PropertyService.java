package com.stayo.stayo.property.service;

import com.stayo.stayo.common.response.PageResponse;
import com.stayo.stayo.property.dto.request.SearchRequest;
import com.stayo.stayo.property.dto.response.PropertyResponse;

import com.stayo.stayo.property.dto.response.PropertyCardDTO;

import java.util.List;

public interface PropertyService {
    List<PropertyResponse> getFeaturedProperties();
    PageResponse<PropertyCardDTO> searchProperties(String userId, SearchRequest request);
}

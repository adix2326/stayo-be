package com.stayo.stayo.property.service;

import com.stayo.stayo.property.dto.response.CityResponse;

import java.util.List;

public interface CityService {
    List<CityResponse> getPopularCities();
}

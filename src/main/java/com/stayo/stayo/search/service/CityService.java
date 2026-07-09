package com.stayo.stayo.search.service;

import com.stayo.stayo.search.dto.CityResponse;




import java.util.List;

public interface CityService {
    List<CityResponse> getPopularCities();
}

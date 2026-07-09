package com.stayo.stayo.search.service.impl;

import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.search.dto.CityResponse;
import com.stayo.stayo.search.entity.City;
import com.stayo.stayo.search.repository.CityRepository;
import com.stayo.stayo.search.service.CityService;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final PGRepository pgRepository;

    @Override
    public List<CityResponse> getPopularCities() {
        return cityRepository.findByIsActiveTrueAndIsPopularTrue()
                .stream()
                .map(this::mapToCityResponse)
                .collect(Collectors.toList());
    }

    private CityResponse mapToCityResponse(City city){
        long count = pgRepository.countByCityAndIsActiveTrue(city.getName());
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .imageUrl(city.getImageUrl())
                .propertyCount((int) count)
                .build();
    }
}

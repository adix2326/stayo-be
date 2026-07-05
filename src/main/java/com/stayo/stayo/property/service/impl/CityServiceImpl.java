package com.stayo.stayo.property.service.impl;

import com.stayo.stayo.property.dto.response.CityResponse;
import com.stayo.stayo.property.entity.City;
import com.stayo.stayo.property.repository.CityRepository;
import com.stayo.stayo.property.repository.PropertyRepository;
import com.stayo.stayo.property.service.CityService;
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
    private final PropertyRepository propertyRepository;

    @Override
    public List<CityResponse> getPopularCities() {
        return cityRepository.findByIsActiveTrueAndIsPopularTrue()
                .stream()
                .map(this::mapToCityResponse)
                .collect(Collectors.toList());
    }

    private CityResponse mapToCityResponse(City city){
        long count = propertyRepository.countByCityAndIsActiveTrue(city.getName());
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .imageUrl(city.getImageUrl())
                .propertyCount((int) count)
                .build();
    }
}

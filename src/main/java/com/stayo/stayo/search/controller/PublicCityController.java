package com.stayo.stayo.search.controller;

import com.stayo.stayo.search.dto.CityResponse;
import com.stayo.stayo.search.entity.City;
import com.stayo.stayo.search.service.CityService;
import com.stayo.stayo.shared.dto.ApiResponse;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/cities")
@RequiredArgsConstructor
@Tag(name = "Public City API", description = "Public endpoints for city metadata")
public class PublicCityController {
    private final CityService cityService;

    @GetMapping("/popular")
    @Operation(summary = "Get popular cities for landing page")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getPopularCities(){
        List<CityResponse> popularCities = cityService.getPopularCities();
        return ResponseEntity.ok(ApiResponse.success(popularCities, "Popular cities retrieved successfully"));
    }
}

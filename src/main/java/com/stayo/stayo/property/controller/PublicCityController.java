package com.stayo.stayo.property.controller;

import com.stayo.stayo.common.response.ApiResponse;
import com.stayo.stayo.property.dto.response.CityResponse;
import com.stayo.stayo.property.service.CityService;
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

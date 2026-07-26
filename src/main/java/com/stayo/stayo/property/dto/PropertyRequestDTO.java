package com.stayo.stayo.property.dto;

import com.stayo.stayo.shared.enums.Amenity;
import com.stayo.stayo.shared.enums.GenderCategory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequestDTO {

    @NotBlank(message = "PG name is required")
    private String pgName;

    private String description;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Locality is required")
    private String locality;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Gender category is required")
    private GenderCategory genderCategory;

    @NotEmpty(message = "At least one sharing type must be configured")
    @Valid
    private List<SharingTypeRequestDTO> sharingType;

    private List<Amenity> amenities;
}

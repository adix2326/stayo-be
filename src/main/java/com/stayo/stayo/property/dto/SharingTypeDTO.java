package com.stayo.stayo.property.dto;

import com.stayo.stayo.property.enums.RoomSharingType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharingTypeDTO {
    private RoomSharingType type;
    private Double rent;
    private Double deposit;
    private Integer count;
    private Integer occupiedCount;
}

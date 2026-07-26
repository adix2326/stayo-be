package com.stayo.stayo.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenuePointDTO {
    private String month; // short label, e.g. "Jul"
    private Double revenue;
}

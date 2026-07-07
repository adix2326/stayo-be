package com.stayo.stayo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private String id;
    private String name;
    private String profileImage;
    private String city;
    private int wishlistCount;
    private int notificationCount;
}

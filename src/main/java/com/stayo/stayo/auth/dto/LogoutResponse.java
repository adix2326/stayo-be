package com.stayo.stayo.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutResponse {
    private boolean success;
    private String message;
}

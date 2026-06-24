package com.stayo.stayo.auth.controller;

import com.stayo.stayo.auth.dto.UserResponseDto;
import com.stayo.stayo.common.exception.UserNotFoundException;
import com.stayo.stayo.common.security.JwtProvider;
import com.stayo.stayo.common.util.AuthUtil;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "User API Endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AuthUtil authUtil;
    private final UserRepository userRepository;

    @Operation(summary = "Get current user details")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String token){
        log.info("Get current user request");

        String userId = authUtil.extractUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        log.info("User retrieved: {}", userId);
        return ResponseEntity.ok(UserResponseDto.fromUser(user));
    }
}

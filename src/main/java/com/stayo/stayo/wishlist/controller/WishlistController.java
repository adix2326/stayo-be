package com.stayo.stayo.wishlist.controller;

import com.stayo.stayo.auth.util.AuthUtil;
import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.shared.dto.ApiResponse;
import com.stayo.stayo.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Wishlist API", description = "Wishlist management API")
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthUtil authUtil;

    @Operation(summary = "Add property to user wishlist")
    @PostMapping("/add/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String propertyId) {
        log.info("Request to add property {} to wishlist received", propertyId);
        String userId = authUtil.extractUserIdFromToken(token);
        wishlistService.addToWishlist(userId, propertyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Property added to wishlist"));
    }

    @Operation(summary = "Remove property from user wishlist")
    @PostMapping("/remove/{propertyId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String propertyId) {
        log.info("Request to remove property {} from wishlist received", propertyId);
        String userId = authUtil.extractUserIdFromToken(token);
        wishlistService.removeFromWishlist(userId, propertyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Property removed from wishlist"));
    }

    @Operation(summary = "Get user's wishlist properties")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PGCardDTO>>> getWishlist(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("Request to fetch user wishlist received");
        String userId = authUtil.extractUserIdFromToken(token);
        List<PGCardDTO> response = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Wishlist retrieved successfully"));
    }
}

package com.stayo.stayo.wishlist.service;

import com.stayo.stayo.property.dto.PGCardDTO;
import java.util.List;

public interface WishlistService {
    void addToWishlist(String userId, String propertyId);
    void removeFromWishlist(String userId, String propertyId);
    List<PGCardDTO> getWishlist(String userId);
}

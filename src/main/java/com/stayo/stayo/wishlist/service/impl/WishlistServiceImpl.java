package com.stayo.stayo.wishlist.service.impl;

import com.stayo.stayo.property.dto.PGCardDTO;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.exception.UserNotFoundException;
import com.stayo.stayo.user.entity.User;
import com.stayo.stayo.user.repository.UserRepository;
import com.stayo.stayo.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final UserRepository userRepository;
    private final PGRepository pgRepository;

    @Override
    public void addToWishlist(String userId, String propertyId) {
        log.info("Adding property {} to wishlist for user {}", propertyId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> wishlist = user.getWishlistPropertyIds();
        if (wishlist == null) {
            wishlist = new ArrayList<>();
        }
        if (!wishlist.contains(propertyId)) {
            wishlist.add(propertyId);
            user.setWishlistPropertyIds(wishlist);
            userRepository.save(user);
            log.info("Property {} successfully added to user {} wishlist", propertyId, userId);
        } else {
            log.info("Property {} already exists in user {} wishlist", propertyId, userId);
        }
    }

    @Override
    public void removeFromWishlist(String userId, String propertyId) {
        log.info("Removing property {} from wishlist for user {}", propertyId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> wishlist = user.getWishlistPropertyIds();
        if (wishlist != null && wishlist.contains(propertyId)) {
            wishlist.remove(propertyId);
            user.setWishlistPropertyIds(wishlist);
            userRepository.save(user);
            log.info("Property {} successfully removed from user {} wishlist", propertyId, userId);
        } else {
            log.info("Property {} not found in user {} wishlist", propertyId, userId);
        }
    }

    @Override
    public List<PGCardDTO> getWishlist(String userId) {
        log.info("Retrieving wishlist properties for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> wishlistIds = user.getWishlistPropertyIds();
        if (wishlistIds == null || wishlistIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<PG> properties = pgRepository.findAllById(wishlistIds);

        return properties.stream()
                .map(this::mapToPGCardDTO)
                .collect(Collectors.toList());
    }

    private PGCardDTO mapToPGCardDTO(PG property) {
        String thumbnail = (property.getImages() != null && !property.getImages().isEmpty())
                ? property.getImages().get(0)
                : null;

        return PGCardDTO.builder()
                .id(property.getId())
                .name(property.getPgName())
                .thumbnail(thumbnail)
                .city(property.getCity())
                .locality(property.getLocality())
                .rent(property.getRent())
                .rating(property.getRating())
                .reviewCount(property.getReviewCount())
                .verified(true)
                .gender(property.getGenderCategory())
                .distance("1.2 km")
                .wishlist(true)
                .availableBeds(2)
                .ownerVerified(true)
                .build();
    }
}

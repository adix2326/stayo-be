package com.stayo.stayo.property.service.impl;

import com.stayo.stayo.owner.service.OwnerProfileService;
import com.stayo.stayo.owner.exception.OwnerNotVerifiedException;
import com.stayo.stayo.property.dto.PGResponse;
import com.stayo.stayo.property.dto.PropertyRequestDTO;
import com.stayo.stayo.property.dto.SharingTypeRequestDTO;
import com.stayo.stayo.property.entity.Image;
import com.stayo.stayo.property.entity.PG;
import com.stayo.stayo.property.entity.PGImages;
import com.stayo.stayo.property.enums.RoomSharingType;
import com.stayo.stayo.property.exception.PropertyAccessDeniedException;
import com.stayo.stayo.property.exception.TooManyImagesException;
import com.stayo.stayo.property.repository.PGImagesRepository;
import com.stayo.stayo.property.repository.PGRepository;
import com.stayo.stayo.shared.enums.Amenity;
import com.stayo.stayo.shared.enums.GenderCategory;
import com.stayo.stayo.shared.exception.PropertyNotFoundException;
import com.stayo.stayo.storage.dto.StoredFile;
import com.stayo.stayo.storage.service.FileStorageService;
import com.stayo.stayo.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for the owner-facing property management methods added to {@link PGServiceImpl}
 * (createProperty, updateProperty, deactivateProperty, uploadPropertyImage, getMyProperties).
 */
@ExtendWith(MockitoExtension.class)
class PGServiceImplTest {

    @Mock private PGRepository pgRepository;
    @Mock private PGImagesRepository pgImagesRepository;
    @Mock private UserRepository userRepository;
    @Mock private OwnerProfileService ownerProfileService;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks private PGServiceImpl pgService;

    private static final String OWNER_ID = "owner123";
    private static final String OTHER_OWNER_ID = "owner456";
    private static final String PG_ID = "pg789";

    private PropertyRequestDTO validRequest() {
        return PropertyRequestDTO.builder()
                .pgName("Sunrise PG")
                .description("Cozy PG near IT park")
                .city("Pune")
                .locality("Hinjewadi")
                .address("Near Phase 1")
                .genderCategory(GenderCategory.UNISEX)
                .sharingType(List.of(
                        SharingTypeRequestDTO.builder().type(RoomSharingType.SINGLE).rent(9000.0).deposit(9000.0).count(4).build(),
                        SharingTypeRequestDTO.builder().type(RoomSharingType.DOUBLE).rent(7000.0).deposit(7000.0).count(4).build()
                ))
                .amenities(List.of(Amenity.WIFI, Amenity.AC))
                .build();
    }

    private PG existingOwnedPg() {
        return PG.builder()
                .id(PG_ID)
                .pgName("Old Name")
                .ownerId(OWNER_ID)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("createProperty")
    class CreatePropertyTests {

        @Test
        @DisplayName("Approved owner — creates and returns the property")
        void createProperty_approvedOwner() {
            when(ownerProfileService.isApprovedOwner(OWNER_ID)).thenReturn(true);
            when(pgRepository.save(any(PG.class))).thenAnswer(inv -> inv.getArgument(0));

            PGResponse response = pgService.createProperty(OWNER_ID, validRequest());

            assertEquals("Sunrise PG", response.getPgName());
            verify(pgRepository).save(any(PG.class));
        }

        @Test
        @DisplayName("Unverified owner — throws OwnerNotVerifiedException")
        void createProperty_unverifiedOwner() {
            when(ownerProfileService.isApprovedOwner(OWNER_ID)).thenReturn(false);

            assertThrows(OwnerNotVerifiedException.class,
                    () -> pgService.createProperty(OWNER_ID, validRequest()));
            verify(pgRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateProperty")
    class UpdatePropertyTests {

        @Test
        @DisplayName("Owner matches — updates and returns the property")
        void updateProperty_owned() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));
            when(pgRepository.save(any(PG.class))).thenAnswer(inv -> inv.getArgument(0));

            PGResponse response = pgService.updateProperty(OWNER_ID, PG_ID, validRequest());

            assertEquals("Sunrise PG", response.getPgName());
        }

        @Test
        @DisplayName("Different owner — throws PropertyAccessDeniedException")
        void updateProperty_notOwned() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));

            assertThrows(PropertyAccessDeniedException.class,
                    () -> pgService.updateProperty(OTHER_OWNER_ID, PG_ID, validRequest()));
        }

        @Test
        @DisplayName("Non-existent property — throws PropertyNotFoundException")
        void updateProperty_notFound() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.empty());

            assertThrows(PropertyNotFoundException.class,
                    () -> pgService.updateProperty(OWNER_ID, PG_ID, validRequest()));
        }
    }

    @Nested
    @DisplayName("deactivateProperty")
    class DeactivatePropertyTests {

        @Test
        @DisplayName("Owner matches — sets isActive false")
        void deactivateProperty_owned() {
            PG pg = existingOwnedPg();
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(pg));
            when(pgRepository.save(any(PG.class))).thenAnswer(inv -> inv.getArgument(0));

            pgService.deactivateProperty(OWNER_ID, PG_ID);

            assertFalse(pg.getIsActive());
        }

        @Test
        @DisplayName("Different owner — throws PropertyAccessDeniedException")
        void deactivateProperty_notOwned() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));

            assertThrows(PropertyAccessDeniedException.class,
                    () -> pgService.deactivateProperty(OTHER_OWNER_ID, PG_ID));
        }
    }

    @Nested
    @DisplayName("getMyProperties")
    class GetMyPropertiesTests {

        @Test
        @DisplayName("Returns properties owned by the caller")
        void getMyProperties_returnsOwned() {
            when(pgRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(existingOwnedPg()));

            List<PGResponse> result = pgService.getMyProperties(OWNER_ID);

            assertEquals(1, result.size());
            assertEquals(PG_ID, result.get(0).getId());
        }
    }

    @Nested
    @DisplayName("uploadPropertyImage")
    class UploadPropertyImageTests {

        private MultipartFile validFile() {
            return new MockMultipartFile("file", "room.jpg", "image/jpeg", "content".getBytes());
        }

        @Test
        @DisplayName("First image — uploads, sets as cover, sortOrder 0")
        void uploadPropertyImage_firstImage_isCover() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));
            when(pgImagesRepository.findByPgId(PG_ID)).thenReturn(Optional.empty());
            StoredFile stored = new StoredFile(
                    "https://res.cloudinary.com/demo/image/upload/v1/property-images/abc.jpg", "property-images/abc");
            when(fileStorageService.upload(any(MultipartFile.class), eq("property-images"))).thenReturn(stored);
            when(pgImagesRepository.save(any(PGImages.class))).thenAnswer(inv -> inv.getArgument(0));

            String url = pgService.uploadPropertyImage(OWNER_ID, PG_ID, validFile());

            assertEquals(stored.url(), url);
            org.mockito.ArgumentCaptor<PGImages> captor = org.mockito.ArgumentCaptor.forClass(PGImages.class);
            verify(pgImagesRepository).save(captor.capture());
            Image saved = captor.getValue().getImages().get(0);
            assertEquals("property-images/abc", saved.getFileId());
            assertEquals(stored.url(), saved.getFileUrl());
            assertEquals(0, saved.getSortOrder());
            assertTrue(saved.getIsCoverImage());
        }

        @Test
        @DisplayName("Additional image — not cover, sortOrder follows existing count")
        void uploadPropertyImage_additionalImage_notCover() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));
            List<Image> existing = new ArrayList<>(List.of(
                    Image.builder().fileId("property-images/first").fileUrl("url1").sortOrder(0).isCoverImage(true).build()
            ));
            PGImages pgImages = PGImages.builder().pgId(PG_ID).images(existing).build();
            when(pgImagesRepository.findByPgId(PG_ID)).thenReturn(Optional.of(pgImages));
            StoredFile stored = new StoredFile(
                    "https://res.cloudinary.com/demo/image/upload/v1/property-images/second.jpg", "property-images/second");
            when(fileStorageService.upload(any(MultipartFile.class), eq("property-images"))).thenReturn(stored);
            when(pgImagesRepository.save(any(PGImages.class))).thenAnswer(inv -> inv.getArgument(0));

            pgService.uploadPropertyImage(OWNER_ID, PG_ID, validFile());

            Image added = pgImages.getImages().get(1);
            assertEquals(1, added.getSortOrder());
            assertFalse(added.getIsCoverImage());
        }

        @Test
        @DisplayName("At MAX_IMAGES_PER_PG cap — throws TooManyImagesException, no storage call")
        void uploadPropertyImage_atCap_throwsTooManyImages() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));
            List<Image> tenImages = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                tenImages.add(Image.builder().fileId("img" + i).sortOrder(i).isCoverImage(i == 0).build());
            }
            PGImages pgImages = PGImages.builder().pgId(PG_ID).images(tenImages).build();
            when(pgImagesRepository.findByPgId(PG_ID)).thenReturn(Optional.of(pgImages));

            assertThrows(TooManyImagesException.class, () ->
                    pgService.uploadPropertyImage(OWNER_ID, PG_ID, validFile()));
            verifyNoInteractions(fileStorageService);
            verify(pgImagesRepository, never()).save(any());
        }

        @Test
        @DisplayName("Non-owner caller — throws PropertyAccessDeniedException")
        void uploadPropertyImage_wrongOwner_throwsAccessDenied() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));

            assertThrows(PropertyAccessDeniedException.class, () ->
                    pgService.uploadPropertyImage(OTHER_OWNER_ID, PG_ID, validFile()));
            verifyNoInteractions(fileStorageService);
        }

        @Test
        @DisplayName("Empty file — throws IllegalArgumentException")
        void uploadPropertyImage_emptyFile_throwsIllegalArgumentException() {
            when(pgRepository.findById(PG_ID)).thenReturn(Optional.of(existingOwnedPg()));
            MultipartFile empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

            assertThrows(IllegalArgumentException.class, () ->
                    pgService.uploadPropertyImage(OWNER_ID, PG_ID, empty));
            verifyNoInteractions(fileStorageService);
        }
    }
}

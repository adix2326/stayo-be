package com.stayo.stayo.booking.mapper;

import com.stayo.stayo.booking.dto.BookingResponseDTO;
import com.stayo.stayo.booking.dto.OccupantDTO;
import com.stayo.stayo.booking.entity.Booking;
import com.stayo.stayo.booking.entity.OccupantInfo;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Booking entity to BookingResponseDTO.
 * Kept intentionally simple — no MapStruct dependency.
 */
@Component
public class BookingMapper {

    public BookingResponseDTO toResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .pgId(booking.getPgId())
                .pgName(booking.getPgName())
                .pgLocality(booking.getPgLocality())
                .pgCity(booking.getPgCity())
                .roomType(booking.getRoomType())
                .moveInDate(booking.getMoveInDate())
                .minimumStay(booking.getMinimumStay())
                .occupantCount(booking.getOccupantCount())
                .primaryOccupant(toOccupantDTO(booking.getPrimaryOccupant()))
                .extraOccupants(toOccupantDTOList(booking.getExtraOccupants()))
                .specialNote(booking.getSpecialNote())
                .rejectionReason(booking.getRejectionReason())
                .monthlyRent(booking.getMonthlyRent())
                .securityDeposit(booking.getSecurityDeposit())
                .totalPayable(booking.getTotalPayable())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public List<BookingResponseDTO> toResponseDTOList(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return Collections.emptyList();
        }
        return bookings.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private OccupantDTO toOccupantDTO(OccupantInfo info) {
        if (info == null) return null;
        return OccupantDTO.builder()
                .name(info.getName())
                .phone(info.getPhone())
                .email(info.getEmail())
                .build();
    }

    private List<OccupantDTO> toOccupantDTOList(List<OccupantInfo> infoList) {
        if (infoList == null || infoList.isEmpty()) {
            return Collections.emptyList();
        }
        return infoList.stream()
                .map(this::toOccupantDTO)
                .collect(Collectors.toList());
    }
}

package com.example.Autovermietung.dto.reservation;

import com.example.Autovermietung.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long autoId,
        String autoBrand,
        String autoLicensePlate,
        Long userId,
        String userEmail,
        ReservationStatus status,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        BigDecimal totalPrice
) {
}

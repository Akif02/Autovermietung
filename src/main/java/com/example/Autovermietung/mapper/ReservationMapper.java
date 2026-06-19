package com.example.Autovermietung.mapper;

import com.example.Autovermietung.entities.Reservation;
import com.example.Autovermietung.dto.reservation.ReservationResponse;

public class ReservationMapper {

    private ReservationMapper() {
    }

    public static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getAuto() != null ? reservation.getAuto().getId() : null,
                reservation.getAuto() != null ? reservation.getAuto().getBrand() : null,
                reservation.getAuto() != null ? reservation.getAuto().getLicensePlate() : null,
                reservation.getUser() != null ? reservation.getUser().getId() : null,
                reservation.getUser() != null ? reservation.getUser().getEmail() : null,
                reservation.getStatus(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getTotalPrice()
        );
    }
}

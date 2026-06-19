package com.example.Autovermietung.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotNull Long autoId,
        
        @NotNull 
        @FutureOrPresent 
        @JsonFormat(pattern = "dd.MM.yyyy HH:mm") // Deutsches Format: 01.06.2024 10:00
        LocalDateTime startDateTime,
        
        @NotNull 
        @JsonFormat(pattern = "dd.MM.yyyy HH:mm") // Deutsches Format: 01.06.2024 10:00
        LocalDateTime endDateTime
) {
}

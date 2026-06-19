package com.example.Autovermietung.dto.user;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        boolean hasDriverLicense,
        LocalDate birthDate,
        String role
) {
}

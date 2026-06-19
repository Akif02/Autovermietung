package com.example.Autovermietung.dto.auto;

import com.example.Autovermietung.enums.CarStatus;
import com.example.Autovermietung.enums.Category;
import com.example.Autovermietung.enums.Fuel;

import java.math.BigDecimal;

public record AutoResponse (
        Long id,
        String brand,
        Category category,
        Integer seats,
        BigDecimal pricePerDay,
        String transmission,
        String licensePlate,
        CarStatus status,
        Fuel fuel
)
{}


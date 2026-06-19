package com.example.Autovermietung.mapper;

import com.example.Autovermietung.entities.Auto;
import com.example.Autovermietung.dto.auto.AutoResponse;
import com.example.Autovermietung.dto.auto.CreateAutoRequest;
import com.example.Autovermietung.dto.auto.UpdateAutoRequest;

public final class AutoMapper {

    private AutoMapper(){

    }

    public static Auto toEntity(CreateAutoRequest request){
        Auto auto = new Auto();

        auto.setBrand(request.brand());
        auto.setCategory(request.category());
        auto.setSeats(request.seats());
        auto.setPricePerDay(request.pricePerDay());
        auto.setTransmission(request.transmission());
        auto.setLicensePlate(request.licensePlate());
        auto.setStatus(request.status());
        auto.setFuel(request.fuel());

        return auto;
    }

    public static void updateEntity(Auto auto, UpdateAutoRequest request){
        auto.setPricePerDay(request.pricePerDay());
        auto.setStatus(request.status());
        auto.setLicensePlate(request.licensePlate());
    }

    public static  AutoResponse responseEntity(Auto auto){
        return new AutoResponse(
                auto.getId(),
                auto.getBrand(),
                auto.getCategory(),
                auto.getSeats(),
                auto.getPricePerDay(),
                auto.getTransmission(),
                auto.getLicensePlate(),
                auto.getStatus(),
                auto.getFuel()
        );
    }
}

package com.example.Autovermietung.entities;

import com.example.Autovermietung.enums.CarStatus;
import com.example.Autovermietung.enums.Category;
import com.example.Autovermietung.enums.Fuel;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Auto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String brand;
    @Enumerated(EnumType.STRING)
    private Category category;
    @Column(nullable = false)
    private Integer seats;
    @Column(nullable = false)
    private BigDecimal pricePerDay;
    @Column(nullable = false)
    private String transmission;
    @Column(nullable = false,unique = true)
    private String licensePlate;
    @Column(nullable = false,updatable = true)
    private CarStatus status;
    @Enumerated(EnumType.STRING)
    private Fuel fuel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public CarStatus getStatus() {
        return status;
    }

    public void setStatus(CarStatus status) {
        this.status = status;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Fuel getFuel() {
        return fuel;
    }

    public void setFuel(Fuel fuel) {
        this.fuel = fuel;
    }
}

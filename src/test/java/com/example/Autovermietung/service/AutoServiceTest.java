package com.example.Autovermietung.service;

import com.example.Autovermietung.dto.auto.AutoResponse;
import com.example.Autovermietung.dto.auto.CreateAutoRequest;
import com.example.Autovermietung.entities.Auto;
import com.example.Autovermietung.enums.Category;
import com.example.Autovermietung.enums.Fuel;
import com.example.Autovermietung.enums.CarStatus;
import com.example.Autovermietung.repository.AutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoServiceTest {

    @Mock
    private AutoRepository autoRepository;

    @InjectMocks
    private AutoService autoService;

    private Auto testAuto;

    @BeforeEach
    void setUp() {
        testAuto = new Auto();
        testAuto.setId(1L);
        testAuto.setBrand("Tesla Model S");
        testAuto.setCategory(Category.LIMOUSINE);
        testAuto.setFuel(Fuel.ELEKTRO);
        testAuto.setPricePerDay(new BigDecimal("189.00"));
        testAuto.setSeats(5);
        testAuto.setTransmission("Automatik");
        testAuto.setLicensePlate("M-TE 1234");
        testAuto.setStatus(CarStatus.AVAILABLE);
    }

    @Test
    void create_ShouldReturnAutoResponse_WhenLicensePlateIsUnique() {
        // Arrange
        CreateAutoRequest request = new CreateAutoRequest(
                "Tesla Model S",
                Category.LIMOUSINE,
                5,
                new BigDecimal("189.00"),
                "Automatik",
                "M-TE 1234",
                CarStatus.AVAILABLE,
                Fuel.ELEKTRO
        );

        when(autoRepository.existsByLicensePlate("M-TE 1234")).thenReturn(false);
        when(autoRepository.save(any(Auto.class))).thenReturn(testAuto);

        // Act
        AutoResponse response = autoService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals("Tesla Model S", response.brand());
        assertEquals(CarStatus.AVAILABLE, response.status());
        verify(autoRepository, times(1)).save(any(Auto.class));
    }

    @Test
    void create_ShouldThrowException_WhenLicensePlateExists() {
        // Arrange
        CreateAutoRequest request = new CreateAutoRequest(
                "Tesla", Category.LIMOUSINE, 5, new BigDecimal("100.00"), "Automatik", "M-TE 1234", CarStatus.AVAILABLE, Fuel.ELEKTRO
        );
        when(autoRepository.existsByLicensePlate("M-TE 1234")).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> autoService.create(request));
        verify(autoRepository, never()).save(any(Auto.class));
    }

    @Test
    void newPrice_ShouldUpdatePrice_WhenAutoExists() {
        // Arrange
        when(autoRepository.findById(1L)).thenReturn(Optional.of(testAuto));
        when(autoRepository.save(any(Auto.class))).thenReturn(testAuto);

        // Act
        AutoResponse response = autoService.newPrice(1L, new BigDecimal("199.99"));

        // Assert
        assertEquals(new BigDecimal("199.99"), testAuto.getPricePerDay());
        verify(autoRepository).save(testAuto);
    }

    @Test
    void newPrice_ShouldThrowException_WhenAutoDoesNotExist() {
        // Arrange
        when(autoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> autoService.newPrice(99L, new BigDecimal("100.0")));
    }
}

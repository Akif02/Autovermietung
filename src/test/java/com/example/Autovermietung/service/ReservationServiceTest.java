package com.example.Autovermietung.service;

import com.example.Autovermietung.dto.reservation.CreateReservationRequest;
import com.example.Autovermietung.dto.reservation.ReservationResponse;
import com.example.Autovermietung.entities.Auto;
import com.example.Autovermietung.entities.Reservation;
import com.example.Autovermietung.entities.User;
import com.example.Autovermietung.enums.CarStatus;
import com.example.Autovermietung.enums.ReservationStatus;
import com.example.Autovermietung.repository.AutoRepository;
import com.example.Autovermietung.repository.ReservationRepository;
import com.example.Autovermietung.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private AutoRepository autoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Auto testAuto;
    private User testUser;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testAuto = new Auto();
        testAuto.setId(1L);
        testAuto.setPricePerDay(new BigDecimal("100.0"));
        testAuto.setStatus(CarStatus.AVAILABLE);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.de");

        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setAuto(testAuto);
        testReservation.setUser(testUser);
        testReservation.setStatus(ReservationStatus.BOOKED);
        testReservation.setTotalPrice(new BigDecimal("300.0"));
    }

    @Test
    void book_ShouldCreateReservationAndCalculatePrice_WhenAutoIsAvailable() {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest(
                1L,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 4, 10, 0)
        );

        when(autoRepository.findById(1L)).thenReturn(Optional.of(testAuto));
        when(userRepository.findByEmail("test@test.de")).thenReturn(Optional.of(testUser));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        // Mock Security
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test@test.de", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        ReservationResponse response = reservationService.book(request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("300.0"), response.totalPrice()); // 3 days * 100
        assertEquals(CarStatus.RENTED, testAuto.getStatus());
        verify(autoRepository).save(testAuto);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowException_WhenAutoIsRented() {
        // Arrange
        testAuto.setStatus(CarStatus.RENTED);
        CreateReservationRequest request = new CreateReservationRequest(
                1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );

        when(autoRepository.findById(1L)).thenReturn(Optional.of(testAuto));
        when(userRepository.findByEmail("test@test.de")).thenReturn(Optional.of(testUser));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test@test.de", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> reservationService.book(request));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void cancelReservation_ShouldSetStatusCancelledAndFreeAuto_WhenUserIsOwner() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test@test.de", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        ReservationResponse response = reservationService.cancelReservation(1L);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, testReservation.getStatus());
        assertEquals(CarStatus.AVAILABLE, testAuto.getStatus());
        verify(autoRepository).save(testAuto);
        verify(reservationRepository).save(testReservation);
    }
}

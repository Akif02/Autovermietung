package com.example.Autovermietung.controller;

import com.example.Autovermietung.dto.reservation.CreateReservationRequest;
import com.example.Autovermietung.dto.reservation.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.Autovermietung.service.ReservationService;

import java.util.List;

/**
 * Controller für die Verwaltung von Reservierungen.
 * Bietet Endpunkte zum Erstellen, Abrufen, Stornieren und Löschen von Reservierungen.
 * 
 * @author Akif
 */
@RestController
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    /**
     * Erstellt eine neue Reservierung. (Nur eingeloggte User)
     *
     * @param request Das DTO mit den Reservierungsdaten.
     * @return Die erstellte Reservierung als Response-DTO mit Status 201 Created.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse res = service.book(request);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    /**
     * Ruft alle vorhandenen Reservierungen ab. (Nur Admins)
     *
     * @return Eine Liste aller Reservierungen mit Status 200 OK.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getAll() {
        List<ReservationResponse> alle = service.getAll();
        return new ResponseEntity<>(alle, HttpStatus.OK);
    }

    /**
     * Ruft eine spezifische Reservierung anhand ihrer ID ab. (Eingeloggte User)
     *
     * @param id Die ID der gesuchten Reservierung.
     * @return Die gefundene Reservierung mit Status 200 OK.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> getOne(@PathVariable Long id) {
        ReservationResponse r = service.getOne(id);
        return new ResponseEntity<>(r, HttpStatus.OK);
    }

    /**
     * Storniert eine Reservierung. (Eingeloggte User)
     *
     * @param id Die ID der zu stornierenden Reservierung.
     * @return Die aktualisierte Reservierung mit neuem Status und HTTP 200 OK.
     */
    @PatchMapping("/cancel/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id) {
        ReservationResponse cancelledReservation = service.cancelReservation(id);
        return new ResponseEntity<>(cancelledReservation, HttpStatus.OK);
    }

    /**
     * Löscht eine Reservierung endgültig. (Nur Admins)
     *
     * @param id Die ID der zu löschenden Reservierung.
     * @return Status 204 No Content bei erfolgreicher Löschung.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteReservation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Ruft alle Reservierungen eines bestimmten Benutzers ab. (Eingeloggte User)
     *
     * @param id Die ID des Benutzers.
     * @return Eine Liste der Reservierungen dieses Benutzers mit Status 200 OK.
     */
    @GetMapping("/user/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getByUser(@PathVariable Long id) {
        List<ReservationResponse> alle = service.getByUser(id);
        return new ResponseEntity<>(alle, HttpStatus.OK);
    }
}

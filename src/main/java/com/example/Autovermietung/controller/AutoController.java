package com.example.Autovermietung.controller;

import com.example.Autovermietung.entities.Auto;
import com.example.Autovermietung.dto.auto.AutoResponse;
import com.example.Autovermietung.dto.auto.CreateAutoRequest;
import com.example.Autovermietung.enums.CarStatus;
import com.example.Autovermietung.enums.Category;
import com.example.Autovermietung.enums.Fuel;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.Autovermietung.service.AutoService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller für die Verwaltung von Fahrzeugen (Autos).
 * Bietet Endpunkte zum Erstellen, Suchen, Filtern, Aktualisieren und Löschen von Autos.
 * 
 * @author Akif
 */
@RestController
@RequestMapping("/auto")
public class AutoController {

    private final AutoService service;


    public AutoController(AutoService service) {
        this.service = service;
    }

    /**
     * Erstellt ein neues Fahrzeug im System. (Nur für Admins)
     *
     * @param request Das DTO mit den Fahrzeugdaten (Marke, Modell, Preis, etc.).
     * @return Das erstellte Fahrzeug als Response-DTO mit Status 201 Created.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AutoResponse> create(@Valid @RequestBody CreateAutoRequest request) {
        AutoResponse a1 = service.create(request);
        return new ResponseEntity<>(a1, HttpStatus.CREATED);
    }

    /**
     * Ruft eine Liste aller verfügbaren Fahrzeuge ab. (Öffentlich)
     *
     * @return Eine Liste aller Autos mit Status 200 OK.
     */
    @GetMapping("/all")
    public ResponseEntity<List<AutoResponse>> alleAutos() {
        List<AutoResponse> alle = service.getAll();
        return new ResponseEntity<>(alle, HttpStatus.OK);
    }

    /**
     * Ruft ein spezifisches Fahrzeug anhand seiner ID ab. (Öffentlich)
     *
     * @param id Die ID des gesuchten Fahrzeugs.
     * @return Das gefundene Fahrzeug mit Status 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AutoResponse> getOne(@PathVariable long id) {
        AutoResponse a1 = service.getOne(id);
        return new ResponseEntity<>(a1, HttpStatus.OK);
    }

    /**
     * Sucht Fahrzeuge basierend auf verschiedenen Filterkriterien. (Öffentlich)
     *
     * @param category Die Fahrzeugkategorie (z.B. SUV, Kleinwagen).
     * @param fuel     Die Kraftstoffart (z.B. Benzin, Diesel).
     * @param seats    Die Anzahl der Sitze.
     * @param brand    Die Marke des Fahrzeugs.
     * @return Eine gefilterte Liste von Fahrzeugen als Response-DTOs.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<AutoResponse>> sucheNachFilter(@RequestParam(required = false) Category category,
                                                              @RequestParam(required = false) Fuel fuel,
                                                              @RequestParam(required = false) Integer seats,
                                                              @RequestParam(required = false) String brand) {
        return ResponseEntity.ok(service.searchAutos(category, fuel, seats, brand));
    }

    /**
     * Löscht ein Fahrzeug aus dem System. (Nur für Admins)
     *
     * @param id Die ID des zu löschenden Fahrzeugs.
     * @return Status 204 No Content bei erfolgreicher Löschung.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteCar(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Aktualisiert den Status eines Fahrzeugs. (Nur für Admins)
     *
     * @param id     Die ID des Fahrzeugs.
     * @param status Der neue Status.
     * @return Das aktualisierte Fahrzeug als Response-DTO.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AutoResponse> updateStatus(@PathVariable Long id,
                                                     @RequestParam CarStatus status) {
        AutoResponse updated = service.updateStatus(id, status);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Ändert den Preis pro Tag für ein Fahrzeug. (Nur für Admins)
     *
     * @param id    Die ID des Fahrzeugs.
     * @param price Der neue Preis pro Tag.
     * @return Das aktualisierte Fahrzeug als Response-DTO.
     */
    @PatchMapping("/{id}/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AutoResponse> changePrice(@PathVariable Long id,
                                                    @RequestParam BigDecimal price) {
        AutoResponse update = service.newPrice(id, price);
        return new ResponseEntity<>(update, HttpStatus.OK);
    }
}

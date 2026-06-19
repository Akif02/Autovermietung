package com.example.Autovermietung.service;

import com.example.Autovermietung.entities.Auto;
import com.example.Autovermietung.dto.auto.AutoResponse;
import com.example.Autovermietung.dto.auto.CreateAutoRequest;
import com.example.Autovermietung.enums.CarStatus;
import com.example.Autovermietung.enums.Category;
import com.example.Autovermietung.enums.Fuel;
import com.example.Autovermietung.mapper.AutoMapper;
import com.example.Autovermietung.repository.AutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service-Klasse für die Verwaltung von Fahrzeugen.
 * Beinhaltet Logik zum Erstellen, Suchen, Filtern, Aktualisieren und Löschen von Autos.
 * 
 * @author Akif
 */
@Service
public class AutoService {

    private final AutoRepository repo;

    public AutoService(AutoRepository repo) {
        this.repo = repo;
    }

    /**
     * Erstellt ein neues Fahrzeug im System.
     * Prüft, ob das Kennzeichen bereits existiert.
     *
     * @param neuesAuto Das DTO mit den Fahrzeugdaten.
     * @return Das erstellte Fahrzeug als Response-DTO.
     * @throws ResponseStatusException (409 CONFLICT), wenn das Kennzeichen bereits vergeben ist.
     */
    public AutoResponse create(CreateAutoRequest neuesAuto) {
        if (repo.existsByLicensePlate(neuesAuto.licensePlate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto exists already");
        }
        Auto entity = AutoMapper.toEntity(neuesAuto);
        Auto auto = repo.save(entity);
        return AutoMapper.responseEntity(auto);
    }

    /**
     * Ruft alle Fahrzeuge aus der Datenbank ab.
     *
     * @return Eine Liste aller Fahrzeuge.
     */
    public List<AutoResponse> getAll() {
        return repo.findAll().stream()
                .map(AutoMapper::responseEntity)
                .toList();
    }

    /**
     * Sucht ein Fahrzeug anhand seiner ID.
     *
     * @param id Die ID des gesuchten Fahrzeugs.
     * @return Das gefundene Fahrzeug.
     * @throws ResponseStatusException (404 NOT FOUND), wenn das Fahrzeug nicht existiert.
     */
    public AutoResponse getOne(long id) {
        Auto auto = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto not found"));
        return AutoMapper.responseEntity(auto);
    }

    /**
     * Sucht Fahrzeuge basierend auf verschiedenen Filterkriterien.
     * Führt Validierungen für Markennamen und Sitzanzahl durch.
     *
     * @param category Die Fahrzeugkategorie (optional).
     * @param fuel     Die Kraftstoffart (optional).
     * @param seats    Die Anzahl der Sitze (optional).
     * @param brand    Die Marke des Fahrzeugs (optional).
     * @return Eine Liste der gefundenen Fahrzeuge als Response-DTOs.
     * @throws ResponseStatusException (400 BAD REQUEST), wenn die Sitzanzahl ungültig ist (<= 0).
     */
    public List<AutoResponse> searchAutos(Category category, Fuel fuel, Integer seats, String brand) {

        // TODO 1: brand trimmen + leere Strings zu null machen
        if (brand != null) {
            brand = brand.trim();
            if (brand.isBlank()) {
                brand = null;
            }
        }
        // TODO 2: seats validieren (z.B. <= 0 -> 400)
        if (seats != null && seats <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seats must be > 0");
        }
        // TODO 3: eine Repository-Methode aufrufen, die alle Filter kombiniert
        List<Auto> autos = repo.search(category, fuel, seats, brand);

        return autos.stream()
                .map(AutoMapper::responseEntity)
                .toList();
    }

    /**
     * Löscht ein Fahrzeug aus der Datenbank.
     *
     * @param id Die ID des zu löschenden Fahrzeugs.
     * @throws ResponseStatusException (404 NOT FOUND), wenn das Fahrzeug nicht existiert.
     */
    public void deleteCar(long id) {
        Auto auto = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto nicht gefunden"));

        repo.delete(auto);
    }

    /**
     * Aktualisiert den Status eines Fahrzeugs.
     *
     * @param id     Die ID des Fahrzeugs.
     * @param status Der neue Status (z.B. VERFÜGBAR, VERMIETET).
     * @return Das aktualisierte Fahrzeug als Response-DTO.
     * @throws ResponseStatusException (404 NOT FOUND), wenn das Fahrzeug nicht existiert.
     */
    public AutoResponse updateStatus(Long id, CarStatus status) {
        Auto a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto not found"));
        a.setStatus(status);
        Auto saved = repo.save(a);
        return AutoMapper.responseEntity(saved);
    }

    /**
     * Ändert den Preis pro Tag für ein Fahrzeug.
     *
     * @param id         Die ID des Fahrzeugs.
     * @param neuerPreis Der neue Preis pro Tag.
     * @return Das aktualisierte Fahrzeug als Response-DTO.
     * @throws ResponseStatusException (404 NOT FOUND), wenn das Fahrzeug nicht existiert.
     */
    public AutoResponse newPrice(Long id, BigDecimal neuerPreis) {
        Auto a = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto not found"));
        a.setPricePerDay(neuerPreis);
        Auto saved = repo.save(a);
        return AutoMapper.responseEntity(saved);
    }
}

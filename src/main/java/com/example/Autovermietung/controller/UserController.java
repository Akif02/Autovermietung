package com.example.Autovermietung.controller;

import com.example.Autovermietung.entities.User;
import com.example.Autovermietung.dto.user.CreateUserRequest;
import com.example.Autovermietung.dto.user.UpdateUserRequest;
import com.example.Autovermietung.dto.user.UserResponse;
import com.example.Autovermietung.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller für die Verwaltung von Benutzern.
 * Bietet Endpunkte zum Erstellen, Abrufen, Aktualisieren und Löschen von Benutzern.
 * 
 * @author Akif
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    /**
     * Erstellt einen neuen Benutzer.
     *
     * @param user Das DTO mit den Benutzerdaten (Name, E-Mail, etc.).
     * @return Der erstellte Benutzer als Response-DTO mit Status 201 Created.
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest user) {
        UserResponse erstellt = service.create(user);
        return new ResponseEntity<>(erstellt, HttpStatus.CREATED);
    }

    /**
     * Ruft alle registrierten Benutzer ab.
     *
     * @return Eine Liste aller Benutzer mit Status 302 Found.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> alle = service.getAll();
        return new ResponseEntity<>(alle, HttpStatus.OK);
    }

    /**
     * Ruft einen spezifischen Benutzer anhand seiner ID ab.
     *
     * @param id Die ID des gesuchten Benutzers.
     * @return Der gefundene Benutzer mit Status 302 Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getOne(@PathVariable Long id) {
        UserResponse u = service.getResponseByID(id);
        return new ResponseEntity<>(u, HttpStatus.OK);
    }

    /**
     * Sucht einen Benutzer anhand seiner E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse des gesuchten Benutzers.
     * @return Der gefundene Benutzer als Response-DTO mit Status 302 Found.
     */
    @GetMapping("/email")
    public ResponseEntity<UserResponse> getByEmail(@RequestParam(value = "email") String email) {
        UserResponse u = service.getByEmail(email);
        return new ResponseEntity<>(u, HttpStatus.OK);
    }

    /**
     * Löscht einen Benutzer aus dem System.
     *
     * @param id Die ID des zu löschenden Benutzers.
     * @return Eine Bestätigungsnachricht mit Status 200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>("User wurde gelöscht", HttpStatus.OK);
    }

    /**
     * Aktualisiert die Daten eines bestehenden Benutzers.
     *
     * @param user Das DTO mit den neuen Benutzerdaten.
     * @param id   Die ID des zu aktualisierenden Benutzers.
     * @return Der aktualisierte Benutzer als Response-DTO mit Status 200 OK.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@Valid @RequestBody UpdateUserRequest user, @PathVariable Long id) {
        UserResponse u = service.update(user, id);
        return new ResponseEntity<>(u, HttpStatus.OK);
    }
}

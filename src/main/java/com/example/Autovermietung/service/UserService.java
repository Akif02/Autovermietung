package com.example.Autovermietung.service;

import com.example.Autovermietung.entities.User;
import com.example.Autovermietung.dto.user.CreateUserRequest;
import com.example.Autovermietung.dto.user.UpdateUserRequest;
import com.example.Autovermietung.dto.user.UserResponse;
import com.example.Autovermietung.mapper.UserMapper;
import com.example.Autovermietung.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service-Klasse für die Verwaltung von Benutzern.
 * Beinhaltet Logik zum Erstellen, Suchen, Aktualisieren und Löschen von Benutzern.
 * 
 * @author Akif
 */
@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Erstellt einen neuen Benutzer im System.
     * Prüft, ob die E-Mail-Adresse bereits registriert ist.
     *
     * @param newUser Das DTO mit den Benutzerdaten.
     * @return Der erstellte Benutzer als Response-DTO.
     * @throws ResponseStatusException (409 CONFLICT), wenn die E-Mail bereits existiert.
     */
    public UserResponse create(CreateUserRequest newUser) {
        if (repo.existsByEmail(newUser.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email existiert bereits");
        }

        User entity = UserMapper.toEntity(newUser);
        
        // Passwort hashen, bevor es gespeichert wird!
        String hashedPassword = passwordEncoder.encode(newUser.password());
        entity.setPassword(hashedPassword);

        User saved = repo.save(entity);

        return UserMapper.toResponse(saved);
    }

    /**
     * Sucht einen Benutzer anhand seiner ID.
     *
     * @param id Die ID des gesuchten Benutzers.
     * @return Der gefundene Benutzer.
     * @throws ResponseStatusException (404 NOT FOUND), wenn der Benutzer nicht existiert.
     */
    public User getByID(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Sucht einen Benutzer anhand seiner ID und gibt ihn als Response-DTO zurück.
     *
     * @param id Die ID des gesuchten Benutzers.
     * @return Der gefundene Benutzer als Response-DTO.
     * @throws ResponseStatusException (404 NOT FOUND), wenn der Benutzer nicht existiert.
     */
    public UserResponse getResponseByID(Long id) {
        return UserMapper.toResponse(getByID(id));
    }

    /**
     * Sucht einen Benutzer anhand seiner E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse des gesuchten Benutzers.
     * @return Der gefundene Benutzer als Response-DTO.
     * @throws ResponseStatusException (404 NOT FOUND), wenn der Benutzer nicht existiert.
     */
    public UserResponse getByEmail(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserMapper.toResponse(user);
    }

    /**
     * Ruft alle registrierten Benutzer aus der Datenbank ab.
     *
     * @return Eine Liste aller Benutzer.
     */
    public List<UserResponse> getAll() {
        return repo.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    /**
     * Aktualisiert die Daten eines bestehenden Benutzers.
     *
     * @param request Das DTO mit den neuen Benutzerdaten.
     * @param id      Die ID des zu aktualisierenden Benutzers.
     * @return Der aktualisierte Benutzer als Response-DTO.
     * @throws ResponseStatusException (404 NOT FOUND), wenn der Benutzer nicht existiert.
     */
    public UserResponse update(UpdateUserRequest request, long id) {
        User aktuellerUser = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String currentEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !aktuellerUser.getEmail().equals(currentEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        UserMapper.updateEntity(aktuellerUser, request);

        User saved = repo.save(aktuellerUser);
        return UserMapper.toResponse(aktuellerUser);
    }

    /**
     * Löscht einen Benutzer aus der Datenbank.
     *
     * @param id Die ID des zu löschenden Benutzers.
     * @throws ResponseStatusException (404 NOT FOUND), wenn der Benutzer nicht existiert.
     */
    public void delete(Long id) {
        User u = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String currentEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !u.getEmail().equals(currentEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        repo.delete(u);
    }
}

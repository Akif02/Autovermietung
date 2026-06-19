package com.example.Autovermietung.service;

import com.example.Autovermietung.dto.user.CreateUserRequest;
import com.example.Autovermietung.dto.user.UpdateUserRequest;
import com.example.Autovermietung.dto.user.UserResponse;
import com.example.Autovermietung.entities.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Max Mustermann");
        testUser.setEmail("max@test.de");
        testUser.setPassword("hashedPassword123");
        testUser.setRole("USER");
    }

    @Test
    void create_ShouldReturnUserResponse_WhenEmailIsUnique() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Max Mustermann",
                LocalDate.of(1990, 1, 1),
                "max@test.de",
                "password123",
                "0123456789",
                "Musterstraße 1",
                true
        );

        when(userRepository.existsByEmail("max@test.de")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals("max@test.de", response.email());
        assertEquals("Max Mustermann", response.name());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Max Mustermann", LocalDate.of(1990, 1, 1), "max@test.de",
                "password", "123", "Address", true
        );
        when(userRepository.existsByEmail("max@test.de")).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_ShouldUpdateUser_WhenUserIsOwner() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(
                "Max Neu", LocalDate.of(1990, 1, 1), "max@test.de",
                "987654321", "Neue Str 1", true
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Mock Security Context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "max@test.de", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act
        UserResponse response = userService.update(request, 1L);

        // Assert
        assertEquals("Max Neu", response.name());
        assertEquals("987654321", response.phone());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_ShouldThrowException_WhenUserIsNotOwnerAndNotAdmin() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(
                "Max Neu", LocalDate.of(1990, 1, 1), "max@test.de",
                "987654321", "Neue Str 1", true
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Mock Security Context with different user
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "other@test.de", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.update(request, 1L));
        verify(userRepository, never()).save(any(User.class));
    }
}

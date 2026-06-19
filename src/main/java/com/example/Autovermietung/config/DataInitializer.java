package com.example.Autovermietung.config;

import com.example.Autovermietung.entities.User;
import com.example.Autovermietung.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import com.example.Autovermietung.entities.Auto;
import com.example.Autovermietung.enums.CarStatus;
import com.example.Autovermietung.enums.Category;
import com.example.Autovermietung.enums.Fuel;
import com.example.Autovermietung.repository.AutoRepository;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AutoRepository autoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, AutoRepository autoRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.autoRepository = autoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        if (!userRepository.existsByEmail("admin@test.de")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@test.de");
            
            String encodedPassword = passwordEncoder.encode("admin123");
            admin.setPassword(encodedPassword);
            
            admin.setRole("ADMIN");
            admin.setBirthDate(LocalDate.of(1990, 1, 1));
            admin.setPhone("0123456789");
            admin.setAddress("Admin Street 1");
            admin.setHasDriverLicense(true);

            userRepository.save(admin);
            System.out.println("✅ ADMIN-USER WURDE ERSTELLT: admin@test.de / admin123");
        } else {
            System.out.println("ℹ️ Admin-User existiert bereits.");
        }

        if (autoRepository.count() == 0) {
            Auto a1 = new Auto();
            a1.setBrand("Porsche 911");
            a1.setCategory(Category.COUPE);
            a1.setFuel(Fuel.BENZIN);
            a1.setPricePerDay(new BigDecimal("299.00"));
            a1.setSeats(2);
            a1.setTransmission("Automatik");
            a1.setStatus(CarStatus.AVAILABLE);
            a1.setLicensePlate("M-PO 911");
            autoRepository.save(a1);

            Auto a2 = new Auto();
            a2.setBrand("Tesla Model S");
            a2.setCategory(Category.LIMOUSINE);
            a2.setFuel(Fuel.ELEKTRO);
            a2.setPricePerDay(new BigDecimal("189.00"));
            a2.setSeats(5);
            a2.setTransmission("Automatik");
            a2.setStatus(CarStatus.AVAILABLE);
            a2.setLicensePlate("B-TS 123E");
            autoRepository.save(a2);

            Auto a3 = new Auto();
            a3.setBrand("Mercedes G-Klasse");
            a3.setCategory(Category.SUV);
            a3.setFuel(Fuel.DIESEL);
            a3.setPricePerDay(new BigDecimal("249.00"));
            a3.setSeats(5);
            a3.setTransmission("Automatik");
            a3.setStatus(CarStatus.AVAILABLE);
            a3.setLicensePlate("S-MB 500");
            autoRepository.save(a3);
            
            System.out.println("✅ 3 BASIS-AUTOS WURDEN ERSTELLT.");
        }
    }
}

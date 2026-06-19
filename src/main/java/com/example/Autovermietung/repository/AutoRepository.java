package com.example.Autovermietung.repository;

import com.example.Autovermietung.entities.Auto;
import com.example.Autovermietung.enums.Category;
import com.example.Autovermietung.enums.Fuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AutoRepository extends JpaRepository<Auto,Long> {
    boolean existsByLicensePlate(String licensePlate);

@Query("""
        SELECT a FROM Auto a
        WHERE (:category IS NULL OR a.category = :category)
        AND (:seats IS NULL OR a.seats = :seats)
        AND (:fuel IS NULL OR a.fuel = :fuel)
        AND (:brand IS NULL OR LOWER(a.brand) = LOWER(:brand))
        """)
List<Auto> search(
        @Param("category") Category category,
        @Param("fuel") Fuel fuel,
        @Param("seats") Integer seats,
        @Param("brand") String brand
);
}

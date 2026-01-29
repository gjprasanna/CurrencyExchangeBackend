package com.example.demo.Repository;

import com.example.demo.modal.CurrencyPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyPositionRepository extends JpaRepository<CurrencyPosition, Long> {

    // Find position by currency code
    Optional<CurrencyPosition> findByCurrencyCode(String currencyCode);

    // Check if currency position exists
    boolean existsByCurrencyCode(String currencyCode);
}
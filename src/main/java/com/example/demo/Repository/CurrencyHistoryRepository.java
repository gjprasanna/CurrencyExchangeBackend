package com.example.demo.Repository;

import com.example.demo.modal.CurrencyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyHistoryRepository extends JpaRepository<CurrencyHistory, Long> {

    /**
     * Find historical rate for specific code and date
     */
    Optional<CurrencyHistory> findByCodeAndRateDate(String code, LocalDate rateDate);

    /**
     * Find all historical rates for a currency within date range
     */
    List<CurrencyHistory> findByCodeAndRateDateBetween(
            String code, LocalDate startDate, LocalDate endDate
    );

    /**
     * Find all historical rates for a currency
     */
    List<CurrencyHistory> findByCodeOrderByRateDateDesc(String code);

    /**
     * Check if historical rate exists
     */
    boolean existsByCodeAndRateDate(String code, LocalDate rateDate);
}
//package com.example.demo.Repository;
//
//import com.example.demo.modal.Currency;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//
//import java.util.Optional;
//@Repository
//public interface CurrencyRepository extends JpaRepository<Currency, Long> {
//    Optional<Currency> findByCode(String code);
//    boolean existsByCode(String code);
//}

package com.example.demo.Repository;

import com.example.demo.modal.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    /**
     * Find currency rate for specific code and date
     */
    Optional<Currency> findByCodeAndRateDate(String code, LocalDate rateDate);

    /**
     * Check if rate exists for specific code and date
     */
    boolean existsByCodeAndRateDate(String code, LocalDate rateDate);

    /**
     * Get latest rate for a currency (most recent date)
     */
    @Query("SELECT c FROM Currency c WHERE c.code = :code ORDER BY c.rateDate DESC LIMIT 1")
    Optional<Currency> findLatestByCode(@Param("code") String code);

    /**
     * Get all latest rates (one per currency, most recent date for each)
     */
    @Query("SELECT c FROM Currency c WHERE c.rateDate = " +
            "(SELECT MAX(c2.rateDate) FROM Currency c2 WHERE c2.code = c.code)")
    List<Currency> findAllLatest();

    /**
     * Find all rates for a currency within date range
     */
    List<Currency> findByCodeAndRateDateBetween(
            String code, LocalDate startDate, LocalDate endDate
    );

    /**
     * Find all rates older than cutoff date (for archiving)
     */
    List<Currency> findByRateDateBefore(LocalDate cutoffDate);

    /**
     * Get previous trading day rate (yesterday or last available)
     */
    @Query("SELECT c FROM Currency c WHERE c.code = :code AND c.rateDate < :currentDate " +
            "ORDER BY c.rateDate DESC LIMIT 1")
    Optional<Currency> findPreviousRate(@Param("code") String code,
                                        @Param("currentDate") LocalDate currentDate);

    /**
     * Delete all rates for specific date
     */
    void deleteByRateDate(LocalDate rateDate);
}
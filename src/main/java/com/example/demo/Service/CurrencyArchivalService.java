package com.example.demo.Service;

import com.example.demo.modal.Currency;
import com.example.demo.modal.CurrencyHistory;
import com.example.demo.Repository.CurrencyRepository;
import com.example.demo.Repository.CurrencyHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to automatically archive currency rates older than 30 days
 * Runs daily at 3 AM
 */
@Service
public class CurrencyArchivalService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyArchivalService.class);
    private static final int RETENTION_DAYS = 30;

    private final CurrencyRepository currencyRepository;
    private final CurrencyHistoryRepository historyRepository;

    public CurrencyArchivalService(CurrencyRepository currencyRepository,
                                   CurrencyHistoryRepository historyRepository) {
        this.currencyRepository = currencyRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Archive old currency rates (runs daily at 3 AM)
     * Keeps last 30 days in main table, moves older to history
     */
    @Scheduled(cron = "0 0 3 * * ?") // 3 AM every day
    @Transactional
    public void archiveOldRates() {
        log.info("Starting daily currency archival process");

        LocalDate cutoffDate = LocalDate.now().minusDays(RETENTION_DAYS);
        log.info("Archiving rates older than: {}", cutoffDate);

        try {
            // Find all rates older than cutoff
            List<Currency> oldRates = currencyRepository.findByRateDateBefore(cutoffDate);

            if (oldRates.isEmpty()) {
                log.info("No rates found for archival");
                return;
            }

            log.info("Found {} rates to archive", oldRates.size());

            int archived = 0;
            int skipped = 0;

            for (Currency rate : oldRates) {
                try {
                    // Check if already archived
                    if (historyRepository.existsByCodeAndRateDate(rate.getCode(), rate.getRateDate())) {
                        log.debug("Rate already archived: {} on {}", rate.getCode(), rate.getRateDate());
                        skipped++;
                        continue;
                    }

                    // Convert to history record
                    CurrencyHistory history = convertToHistory(rate);
                    historyRepository.save(history);

                    archived++;
                    log.debug("Archived: {} on {}", rate.getCode(), rate.getRateDate());

                } catch (Exception e) {
                    log.error("Failed to archive rate for {} on {}: {}",
                            rate.getCode(), rate.getRateDate(), e.getMessage());
                }
            }

            // Delete archived rates from main table
            if (archived > 0) {
                currencyRepository.deleteAll(oldRates);
                log.info("Successfully archived {} rates, skipped {} duplicates", archived, skipped);
            }

        } catch (Exception e) {
            log.error("Currency archival process failed", e);
            throw e; // Re-throw to trigger transaction rollback
        }

        log.info("Currency archival process completed");
    }

    /**
     * Convert Currency to CurrencyHistory
     */
    private CurrencyHistory convertToHistory(Currency currency) {
        CurrencyHistory history = new CurrencyHistory();
        history.setCode(currency.getCode());
        history.setName(currency.getName());
        history.setRateDate(currency.getRateDate());
        history.setUploadedAt(currency.getUploadedAt());
        history.setLastModified(currency.getLastModified());
        history.setArchivedAt(LocalDateTime.now());

        // Copy rates
        history.setBnrrate(currency.getBnrrate());
        history.setBuyrate(currency.getBuyrate());
        history.setSellrate(currency.getSellrate());
        history.setBuyspreadrate(currency.getBuyspreadrate());
        history.setSellspreadrate(currency.getSellspreadrate());

        return history;
    }

    /**
     * Manual trigger for archival (useful for testing or manual runs)
     */
    @Transactional
    public void archiveOldRatesManually() {
        log.info("Manual archival triggered");
        archiveOldRates();
    }
}
////package com.example.demo.Service.Impl;
////
////import com.example.demo.Dto.CurrencyDTO;
////import com.example.demo.Mapper.CurrencyMapper;
////import com.example.demo.Modal.Currency;
////import com.example.demo.Repository.CurrencyRepository;
////import com.example.demo.Service.CurrencyService;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.math.BigDecimal;
////import java.util.List;
////import java.util.stream.Collectors;
////
////@Service
////public class CurrencyServiceImpl implements CurrencyService {
////
////    private final CurrencyRepository repository;
////    private final CurrencyMapper mapper;
////
////    public CurrencyServiceImpl(CurrencyRepository repository, CurrencyMapper mapper) {
////        this.repository = repository;
////        this.mapper = mapper;
////    }
////
////    @Override
////    @Transactional
////    public void createCurrency(CurrencyDTO dto) {
////        // Check if currency code already exists
////        if (repository.existsByCode(dto.code())) {
////            throw new IllegalArgumentException("Currency with code " + dto.code() + " already exists");
////        }
////
////        // Use mapper to turn DTO into Entity (includes customerSpread from manager)
////        Currency entity = mapper.toEntity(dto);
////
////        repository.save(entity);
////    }
////
////    @Override
////    public List<CurrencyDTO> getAll() {
////        return repository.findAll().stream()
////                .map(this::mapToDtoWithRates) // Use a helper method
////                .collect(Collectors.toList());
////    }
////
////    @Override
////    public CurrencyDTO getByCode(String code) {
////        return repository.findByCode(code)
////                .map(this::mapToDtoWithRates)
////                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + code));
////    }
////
////    // Helper method to handle the math
////    private CurrencyDTO mapToDtoWithRates(Currency entity) {
////        return new CurrencyDTO(
////                entity.getCode(),
////                entity.getName(),
////                entity.getBnrrate(),
////                entity.getBuyrate(),
////                entity.getSellrate(),
////                entity.getBuyspreadrate(),
////                entity.getSellspreadrate(),
////                // Calculation: buyrate + buyspreadrate
////                entity.getBuyrate().add(entity.getBuyspreadrate()),
////                // Calculation: sellrate + sellspreadrate
////                entity.getSellrate().add(entity.getSellspreadrate())
////        );
////    }
////
////}
//
//
////Jan21->
////
////package com.example.demo.Service.Impl;
////
////import com.example.demo.Dto.CurrencyDTO;
////import com.example.demo.modal.Currency;
////import com.example.demo.Repository.CurrencyRepository;
////import com.example.demo.Service.CurrencyService;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////import org.springframework.transaction.annotation.Propagation;
////
////import java.math.BigDecimal;
////import java.math.RoundingMode;
////import java.time.LocalDate;
////import java.time.LocalDateTime;
////import java.util.List;
////import java.util.stream.Collectors;
////
////@Service
////public class CurrencyServiceImpl implements CurrencyService {
////
////    private final CurrencyRepository repository;
////
////    public CurrencyServiceImpl(CurrencyRepository repository) {
////        this.repository = repository;
////    }
////
////    /**
////     * MAIN UPLOAD LOOP
////     * We don't use @Transactional here so that we can "Keep Going"
////     * even if one box is broken.
////     */
////    @Override
////    public void processDailyUpload(List<CurrencyDTO> dailyRates) {
////        if (dailyRates == null || dailyRates.isEmpty()) return;
////
////        for (CurrencyDTO dto : dailyRates) {
////            try {
////                // Try to save each box one by one
////                saveOrUpdateSingleCurrency(dto);
////            } catch (Exception e) {
////                // If one breaks, just print the error and move to the next one!
////                System.err.println("Failed to process " + dto.code() + ": " + e.getMessage());
////            }
////        }
////    }
////
////    /**
////     * INDIVIDUAL BOX HANDLER
////     * REQUIRES_NEW makes this box independent from the others.
////     */
////    @Transactional(propagation = Propagation.REQUIRES_NEW)
////    public void saveOrUpdateSingleCurrency(CurrencyDTO dto) {
////        Currency currency = repository.findByCode(dto.code())
////                .orElse(new Currency());
////
////        if (currency.getId() != null) {
////            // Box exists! Move current toys to the "Yesterday" shelf.
////            archiveCurrentRates(currency);
////        } else {
////            // New box! Set the birthday.
////            currency.setCode(dto.code());
////            currency.setUploadedAt(LocalDateTime.now());
////        }
////
////        // Put the NEW toys in the box
////        currency.setName(dto.name());
////        currency.setBnrrate(dto.bnrrate());
////        currency.setBuyrate(dto.buyrate());
////        currency.setSellrate(dto.sellrate());
////
////        // "Magic Zero" check: if spread is missing, use 0.00
////        currency.setBuyspreadrate(dto.buyspreadrate() != null ? dto.buyspreadrate() : BigDecimal.ZERO);
////        currency.setSellspreadrate(dto.sellspreadrate() != null ? dto.sellspreadrate() : BigDecimal.ZERO);
////
////        currency.setRateDate(LocalDate.now());
////        currency.setLastModified(LocalDateTime.now());
////
////        repository.save(currency);
////    }
////
////    /**
////     * THE MOVING DAY (Archiving)
////     * This moves Today's data to the "Previous" shelves.
////     */
////    private void archiveCurrentRates(Currency currency) {
////        // Move current values to previous columns
////        currency.setPreviousBnrRate(currency.getBnrrate());
////        currency.setPreviousBuyrate(currency.getBuyrate());
////        currency.setPreviousSellrate(currency.getSellrate());
////        currency.setPreviousBuyspreadrate(currency.getBuyspreadrate());
////        currency.setPreviousSellspreadrate(currency.getSellspreadrate());
////
////        // FIX FOR 500 ERROR: The "Magic Zero" Rule.
////        // We check for "nothing" before adding!
////        BigDecimal currentBuy = (currency.getBuyrate() != null) ? currency.getBuyrate() : BigDecimal.ZERO;
////        BigDecimal currentBuySpread = (currency.getBuyspreadrate() != null) ? currency.getBuyspreadrate() : BigDecimal.ZERO;
////        currency.setPreviousBuyT24Rate(currentBuy.add(currentBuySpread));
////
////        BigDecimal currentSell = (currency.getSellrate() != null) ? currency.getSellrate() : BigDecimal.ZERO;
////        BigDecimal currentSellSpread = (currency.getSellspreadrate() != null) ? currency.getSellspreadrate() : BigDecimal.ZERO;
////        currency.setPreviousSellT24Rate(currentSell.add(currentSellSpread));
////    }
////
////    @Override
////    @Transactional(readOnly = true)
////    public List<CurrencyDTO> getAll() {
////        return repository.findAll().stream()
////                .map(this::mapToDtoWithComparison)
////                .collect(Collectors.toList());
////    }
////
////    @Override
////    @Transactional(readOnly = true)
////    public CurrencyDTO getByCode(String code) {
////        return repository.findByCode(code)
////                .map(this::mapToDtoWithComparison)
////                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + code));
////    }
////
////    @Override
////    @Transactional
////    public void createCurrency(CurrencyDTO dto) {
////        if (repository.existsByCode(dto.code())) {
////            throw new IllegalArgumentException("Currency already exists: " + dto.code());
////        }
////        saveOrUpdateSingleCurrency(dto);
////    }
////
////    /**
////     * MANUAL MAPPING
////     * This turns the Database Box into a DTO for the Screen.
////     */
////    private CurrencyDTO mapToDtoWithComparison(Currency entity) {
////        // Calculating Today's T24 (Today's Buy + Today's Spread)
////        BigDecimal buyT24 = (entity.getBuyrate() != null ? entity.getBuyrate() : BigDecimal.ZERO)
////                .add(entity.getBuyspreadrate() != null ? entity.getBuyspreadrate() : BigDecimal.ZERO);
////        BigDecimal sellT24 = (entity.getSellrate() != null ? entity.getSellrate() : BigDecimal.ZERO)
////                .add(entity.getSellspreadrate() != null ? entity.getSellspreadrate() : BigDecimal.ZERO);
////
////        return new CurrencyDTO(
////                entity.getCode(),
////                entity.getName(),
////                entity.getBnrrate(),
////                entity.getBuyrate(),
////                entity.getSellrate(),
////                entity.getBuyspreadrate(),
////                entity.getSellspreadrate(),
////                buyT24,
////                sellT24,
////                entity.getPreviousBnrRate(),
////                calculateDiff(entity.getBnrrate(), entity.getPreviousBnrRate()),
////                calculatePct(entity.getBnrrate(), entity.getPreviousBnrRate()),
////                entity.getPreviousBuyT24Rate(),
////                calculateDiff(buyT24, entity.getPreviousBuyT24Rate()),
////                calculatePct(buyT24, entity.getPreviousBuyT24Rate()),
////                entity.getPreviousSellT24Rate(),
////                calculateDiff(sellT24, entity.getPreviousSellT24Rate()),
////                calculatePct(sellT24, entity.getPreviousSellT24Rate()),
////                entity.hasHistoricalData()
////        );
////    }
////
////    private BigDecimal calculateDiff(BigDecimal today, BigDecimal yesterday) {
////        // If we don't have yesterday's toy, we can't show a difference!
////        return (today == null || yesterday == null) ? null : today.subtract(yesterday);
////    }
////
////    private BigDecimal calculatePct(BigDecimal today, BigDecimal yesterday) {
////        // Percentage math: (Today - Yesterday) / Yesterday * 100
////        if (today == null || yesterday == null || yesterday.compareTo(BigDecimal.ZERO) == 0) return null;
////        return today.subtract(yesterday)
////                .divide(yesterday, 4, RoundingMode.HALF_UP)
////                .multiply(new BigDecimal("100"))
////                .setScale(2, RoundingMode.HALF_UP);
////    }
////
////
////
////
////
////}
//
////jan22
//package com.example.demo.Service.Impl;
//
//import com.example.demo.Dto.CurrencyDTO;
//import com.example.demo.Dto.UploadSummary;
//import com.example.demo.modal.Currency;
//import com.example.demo.Repository.CurrencyRepository;
//import com.example.demo.Service.CurrencyService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.annotation.Propagation;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class CurrencyServiceImpl implements CurrencyService {
//
//    private static final Logger log = LoggerFactory.getLogger(CurrencyServiceImpl.class);
//    private final CurrencyRepository repository;
//
//    public CurrencyServiceImpl(CurrencyRepository repository) {
//        this.repository = repository;
//    }
//
//    /**
//     * MAIN UPLOAD LOOP with proper error tracking
//     * Returns summary of successes and failures
//     */
//    @Override
//    public UploadSummary processDailyUpload(List<CurrencyDTO> dailyRates) {
//        if (dailyRates == null || dailyRates.isEmpty()) {
//            log.warn("Empty or null daily rates received");
//            return new UploadSummary(new ArrayList<>(), new ArrayList<>());
//        }
//
//        List<String> succeeded = new ArrayList<>();
//        List<String> failed = new ArrayList<>();
//
//        log.info("Starting daily upload for {} currencies", dailyRates.size());
//
//        for (CurrencyDTO dto : dailyRates) {
//            try {
//                // Validate before processing
//                validateCurrencyDTO(dto);
//
//                // Try to save with retry on optimistic lock
//                saveOrUpdateWithRetry(dto, 3);
//                succeeded.add(dto.code());
//
//                log.debug("Successfully processed: {}", dto.code());
//
//            } catch (IllegalArgumentException e) {
//                String errorMsg = dto.code() + ": " + e.getMessage();
//                failed.add(errorMsg);
//                log.error("Validation failed for {}: {}", dto.code(), e.getMessage());
//
//            } catch (ObjectOptimisticLockingFailureException e) {
//                String errorMsg = dto.code() + ": Concurrent modification detected after retries";
//                failed.add(errorMsg);
//                log.error("Optimistic lock failure for {} after retries", dto.code());
//
//            } catch (Exception e) {
//                String errorMsg = dto.code() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage();
//                failed.add(errorMsg);
//                log.error("Failed to process {}", dto.code(), e);
//            }
//        }
//
//        log.info("Daily upload completed. Succeeded: {}, Failed: {}", succeeded.size(), failed.size());
//
//        return new UploadSummary(succeeded, failed);
//    }
//
//    /**
//     * Retry logic for optimistic locking failures
//     */
//    private void saveOrUpdateWithRetry(CurrencyDTO dto, int maxRetries) {
//        int attempt = 0;
//        while (attempt < maxRetries) {
//            try {
//                saveOrUpdateSingleCurrency(dto);
//                return; // Success
//            } catch (ObjectOptimisticLockingFailureException e) {
//                attempt++;
//                if (attempt >= maxRetries) {
//                    throw e; // Give up after max retries
//                }
//                log.warn("Optimistic lock failure for {}, retry {}/{}", dto.code(), attempt, maxRetries);
//                try {
//                    Thread.sleep(100 * attempt); // Exponential backoff
//                } catch (InterruptedException ie) {
//                    Thread.currentThread().interrupt();
//                    throw new RuntimeException("Interrupted during retry", ie);
//                }
//            }
//        }
//    }
//
//    /**
//     * Validate DTO before processing
//     */
//    private void validateCurrencyDTO(CurrencyDTO dto) {
//        if (dto.code() == null || dto.code().trim().isEmpty()) {
//            throw new IllegalArgumentException("Currency code cannot be null or empty");
//        }
//        if (dto.code().length() != 3) {
//            throw new IllegalArgumentException("Currency code must be exactly 3 characters: " + dto.code());
//        }
//        if (dto.name() == null || dto.name().trim().isEmpty()) {
//            throw new IllegalArgumentException("Currency name cannot be null or empty for " + dto.code());
//        }
//        if (dto.bnrrate() == null || dto.bnrrate().compareTo(BigDecimal.ZERO) <= 0) {
//            throw new IllegalArgumentException("BNR rate must be greater than zero for " + dto.code());
//        }
//        if (dto.buyrate() == null || dto.buyrate().compareTo(BigDecimal.ZERO) < 0) {
//            throw new IllegalArgumentException("Buy rate cannot be null or negative for " + dto.code());
//        }
//        if (dto.sellrate() == null || dto.sellrate().compareTo(BigDecimal.ZERO) < 0) {
//            throw new IllegalArgumentException("Sell rate cannot be null or negative for " + dto.code());
//        }
//    }
//
//    /**
//     * INDIVIDUAL BOX HANDLER
//     * REQUIRES_NEW makes this box independent from the others.
//     */
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void saveOrUpdateSingleCurrency(CurrencyDTO dto) {
//        Currency currency = repository.findByCode(dto.code())
//                .orElse(new Currency());
//
//        if (currency.getId() != null) {
//            // Box exists! Move current toys to the "Yesterday" shelf.
//            archiveCurrentRates(currency);
//            log.debug("Archiving previous rates for {}", dto.code());
//        } else {
//            // New box! Set the birthday.
//            currency.setCode(dto.code());
//            currency.setUploadedAt(LocalDateTime.now());
//            log.info("Creating new currency: {}", dto.code());
//        }
//
//        // Put the NEW toys in the box
//        currency.setName(dto.name());
//        currency.setBnrrate(dto.bnrrate());
//        currency.setBuyrate(dto.buyrate());
//        currency.setSellrate(dto.sellrate());
//
//        // "Magic Zero" check: if spread is missing, use 0.00
//        currency.setBuyspreadrate(safeValue(dto.buyspreadrate()));
//        currency.setSellspreadrate(safeValue(dto.sellspreadrate()));
//
//        currency.setRateDate(LocalDate.now());
//        currency.setLastModified(LocalDateTime.now());
//
//        repository.save(currency);
//    }
//
//    /**
//     * THE MOVING DAY (Archiving)
//     * This moves Today's data to the "Previous" shelves.
//     */
//    private void archiveCurrentRates(Currency currency) {
//        // Move current values to previous columns
//        currency.setPreviousBnrRate(currency.getBnrrate());
//        currency.setPreviousBuyrate(currency.getBuyrate());
//        currency.setPreviousSellrate(currency.getSellrate());
//        currency.setPreviousBuyspreadrate(currency.getBuyspreadrate());
//        currency.setPreviousSellspreadrate(currency.getSellspreadrate());
//
//        // Calculate and store previous T24 rates with null-safe addition
//        BigDecimal currentBuy = safeValue(currency.getBuyrate());
//        BigDecimal currentBuySpread = safeValue(currency.getBuyspreadrate());
//        currency.setPreviousBuyT24Rate(currentBuy.add(currentBuySpread));
//
//        BigDecimal currentSell = safeValue(currency.getSellrate());
//        BigDecimal currentSellSpread = safeValue(currency.getSellspreadrate());
//        currency.setPreviousSellT24Rate(currentSell.add(currentSellSpread));
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<CurrencyDTO> getAll() {
//        List<Currency> currencies = repository.findAll();
//        log.debug("Fetching all currencies, found: {}", currencies.size());
//        return currencies.stream()
//                .map(this::mapToDtoWithComparison)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public CurrencyDTO getByCode(String code) {
//        log.debug("Fetching currency by code: {}", code);
//        return repository.findByCode(code)
//                .map(this::mapToDtoWithComparison)
//                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + code));
//    }
//
//    @Override
//    @Transactional
//    public void createCurrency(CurrencyDTO dto) {
//        if (repository.existsByCode(dto.code())) {
//            throw new IllegalArgumentException("Currency already exists: " + dto.code());
//        }
//        validateCurrencyDTO(dto);
//        saveOrUpdateSingleCurrency(dto);
//        log.info("Currency created successfully: {}", dto.code());
//    }
//
//    /**
//     * MANUAL MAPPING
//     * This turns the Database Box into a DTO for the Screen.
//     */
//    private CurrencyDTO mapToDtoWithComparison(Currency entity) {
//        // Calculating Today's T24 with null-safe addition
//        BigDecimal buyT24 = safeAdd(entity.getBuyrate(), entity.getBuyspreadrate());
//        BigDecimal sellT24 = safeAdd(entity.getSellrate(), entity.getSellspreadrate());
//
//        return new CurrencyDTO(
//                entity.getCode(),
//                entity.getName(),
//                entity.getBnrrate(),
//                entity.getBuyrate(),
//                entity.getSellrate(),
//                entity.getBuyspreadrate(),
//                entity.getSellspreadrate(),
//                buyT24,
//                sellT24,
//                entity.getPreviousBnrRate(),
//                calculateDiff(entity.getBnrrate(), entity.getPreviousBnrRate()),
//                calculatePct(entity.getBnrrate(), entity.getPreviousBnrRate()),
//                entity.getPreviousBuyT24Rate(),
//                calculateDiff(buyT24, entity.getPreviousBuyT24Rate()),
//                calculatePct(buyT24, entity.getPreviousBuyT24Rate()),
//                entity.getPreviousSellT24Rate(),
//                calculateDiff(sellT24, entity.getPreviousSellT24Rate()),
//                calculatePct(sellT24, entity.getPreviousSellT24Rate()),
//                entity.hasHistoricalData()
//        );
//    }
//
//    /**
//     * NULL-SAFE HELPERS
//     */
//    private BigDecimal safeValue(BigDecimal value) {
//        return value != null ? value : BigDecimal.ZERO;
//    }
//
//    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
//        return safeValue(a).add(safeValue(b));
//    }
//
//    private BigDecimal calculateDiff(BigDecimal today, BigDecimal yesterday) {
//        // Return null if historical data doesn't exist - don't default to ZERO
//        // This allows UI to display "N/A" instead of "0.00"
//        if (today == null || yesterday == null) {
//            return null;
//        }
//        return today.subtract(yesterday);
//    }
//
//    private BigDecimal calculatePct(BigDecimal today, BigDecimal yesterday) {
//        // Return null if historical data doesn't exist or division would be invalid
//        if (today == null || yesterday == null || yesterday.compareTo(BigDecimal.ZERO) == 0) {
//            return null;
//        }
//        return today.subtract(yesterday)
//                .divide(yesterday, 4, RoundingMode.HALF_UP)
//                .multiply(new BigDecimal("100"))
//                .setScale(2, RoundingMode.HALF_UP);
//    }
//}

package com.example.demo.Service.Impl;

import com.example.demo.Dto.CurrencyDTO;
import com.example.demo.Dto.UploadSummary;
import com.example.demo.modal.Currency;
import com.example.demo.Repository.CurrencyRepository;
import com.example.demo.Service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyServiceImpl.class);
    private final CurrencyRepository repository;

    public CurrencyServiceImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

//    /**
//     * MAIN UPLOAD LOOP with proper error tracking
//     */
//    @Transactional
//    @Override
//    public UploadSummary processDailyUpload(List<CurrencyDTO> dailyRates) {
//        if (dailyRates == null || dailyRates.isEmpty()) {
//            log.warn("Empty or null daily rates received");
//            return new UploadSummary(new ArrayList<>(), new ArrayList<>());
//        }
//
//        List<String> succeeded = new ArrayList<>();
//        List<String> failed = new ArrayList<>();
//
//        log.info("Starting daily upload for {} currencies", dailyRates.size());
//
//        for (CurrencyDTO dto : dailyRates) {
//            try {
//                validateCurrencyDTO(dto);
//                saveOrUpdateWithRetry(dto, 3);
//                succeeded.add(dto.code());
//                log.debug("Successfully processed: {}", dto.code());
//
//            } catch (IllegalArgumentException e) {
//                String errorMsg = dto.code() + ": " + e.getMessage();
//                failed.add(errorMsg);
//                log.error("Validation failed for {}: {}", dto.code(), e.getMessage());
//
//            } catch (ObjectOptimisticLockingFailureException e) {
//                String errorMsg = dto.code() + ": Concurrent modification detected after retries";
//                failed.add(errorMsg);
//                log.error("Optimistic lock failure for {} after retries", dto.code());
//
//            } catch (Exception e) {
//                String errorMsg = dto.code() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage();
//                failed.add(errorMsg);
//                log.error("Failed to process {}", dto.code(), e);
//            }
//        }
//
//        log.info("Daily upload completed. Succeeded: {}, Failed: {}", succeeded.size(), failed.size());
//        return new UploadSummary(succeeded, failed);
//    }

    /**
     * MAIN UPLOAD LOOP - TRUE ALL-OR-NOTHING
     * Either ALL currencies save or NONE save (single transaction)
     */
    /**
     * MAIN UPLOAD LOOP - TRUE ALL-OR-NOTHING with comprehensive validation
     */
    @Transactional
    @Override
    public UploadSummary processDailyUpload(List<CurrencyDTO> dailyRates) {
        if (dailyRates == null || dailyRates.isEmpty()) {
            log.warn("Empty or null daily rates received");
            return new UploadSummary(new ArrayList<>(), new ArrayList<>());
        }

        List<String> succeeded = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        log.info("Starting daily upload for {} currencies", dailyRates.size());

        // Step 1: Validate ALL currencies and collect ALL errors
        for (CurrencyDTO dto : dailyRates) {
            try {
                validateCurrencyDTO(dto);
            } catch (IllegalArgumentException e) {
                String errorMsg = dto.code() + ": " + e.getMessage();
                validationErrors.add(errorMsg);
                log.error("Validation failed for {}: {}", dto.code(), e.getMessage());
            }
        }

        // If ANY validation failed, reject entire upload
        if (!validationErrors.isEmpty()) {
            log.error("Upload rejected due to {} validation errors", validationErrors.size());
            throw new IllegalArgumentException(
                    "Upload rejected - " + validationErrors.size() + " validation error(s): " +
                            String.join("; ", validationErrors)
            );
        }

        log.info("All {} currencies passed validation", dailyRates.size());

        // Step 2: Save ALL currencies (single transaction)
        try {
            for (CurrencyDTO dto : dailyRates) {
                saveOrUpdateSingleCurrency(dto);
                succeeded.add(dto.code());
                log.debug("Successfully processed: {}", dto.code());
            }

            log.info("Daily upload completed successfully. All {} currencies saved", succeeded.size());
            return new UploadSummary(succeeded, new ArrayList<>());

        } catch (Exception e) {
            log.error("Upload failed during save operation, rolling back entire upload", e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Save or update currency rate for today
     * REMOVED REQUIRES_NEW - now participates in parent transaction
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveOrUpdateSingleCurrency(CurrencyDTO dto) {
        LocalDate today = LocalDate.now();

        // Check if today's rate already exists
        Currency currency = repository.findByCodeAndRateDate(dto.code(), today)
                .orElse(new Currency());

        if (currency.getId() == null) {
            // New record for today
            currency.setCode(dto.code());
            currency.setRateDate(today);
            currency.setUploadedAt(LocalDateTime.now());
            log.info("Creating new rate record for {} on {}", dto.code(), today);
        } else {
            log.info("Updating existing rate for {} on {}", dto.code(), today);
        }

        // Set rates
        currency.setName(dto.name());
        currency.setBnrrate(dto.bnrrate());
        currency.setBuyrate(dto.buyrate());
        currency.setSellrate(dto.sellrate());
        currency.setBuyspreadrate(safeValue(dto.buyspreadrate()));
        currency.setSellspreadrate(safeValue(dto.sellspreadrate()));
        currency.setLastModified(LocalDateTime.now());

        repository.save(currency);
    }

    /**
     * Retry logic for optimistic locking failures
     */
    private void saveOrUpdateWithRetry(CurrencyDTO dto, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                saveOrUpdateSingleCurrency(dto);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw e;
                }
                log.warn("Optimistic lock failure for {}, retry {}/{}", dto.code(), attempt, maxRetries);
                try {
                    Thread.sleep(100 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    /**
     * Validate DTO before processing
     */
    private void validateCurrencyDTO(CurrencyDTO dto) {
        if (dto.code() == null || dto.code().trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
        if (dto.code().length() != 3) {
            throw new IllegalArgumentException("Currency code must be exactly 3 characters: " + dto.code());
        }
        if (dto.name() == null || dto.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Currency name cannot be null or empty for " + dto.code());
        }
        if (dto.bnrrate() == null || dto.bnrrate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("BNR rate must be greater than zero for " + dto.code());
        }
        if (dto.buyrate() == null || dto.buyrate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Buy rate cannot be null or negative for " + dto.code());
        }
        if (dto.sellrate() == null || dto.sellrate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sell rate cannot be null or negative for " + dto.code());
        }
        // ADD THESE TWO CHECKS:
        if (dto.buyspreadrate() == null || dto.buyspreadrate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Buy spread rate cannot be null or negative for " + dto.code());
        }
        if (dto.sellspreadrate() == null || dto.sellspreadrate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sell spread rate cannot be null or negative for " + dto.code());
        }
    }

    /**
     * Save or update currency rate for today
     * Creates NEW record each day instead of updating existing
     */

//    public void saveOrUpdateSingleCurrency(CurrencyDTO dto) {
//        LocalDate today = LocalDate.now();
//
//        // Check if today's rate already exists
//        Currency currency = repository.findByCodeAndRateDate(dto.code(), today)
//                .orElse(new Currency());
//
//        if (currency.getId() == null) {
//            // New record for today
//            currency.setCode(dto.code());
//            currency.setRateDate(today);
//            currency.setUploadedAt(LocalDateTime.now());
//            log.info("Creating new rate record for {} on {}", dto.code(), today);
//        } else {
//            log.info("Updating existing rate for {} on {}", dto.code(), today);
//        }
//
//        // Set rates
//        currency.setName(dto.name());
//        currency.setBnrrate(dto.bnrrate());
//        currency.setBuyrate(dto.buyrate());
//        currency.setSellrate(dto.sellrate());
//        currency.setBuyspreadrate(safeValue(dto.buyspreadrate()));
//        currency.setSellspreadrate(safeValue(dto.sellspreadrate()));
//        currency.setLastModified(LocalDateTime.now());
//
//        repository.save(currency);
//    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyDTO> getAll() {
        List<Currency> currencies = repository.findAllLatest();
        log.debug("Fetching all latest currencies, found: {}", currencies.size());
        return currencies.stream()
                .map(this::mapToDtoWithComparison)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyDTO getByCode(String code) {
        log.debug("Fetching currency by code: {}", code);

        // Get today's rate
        Currency today = repository.findLatestByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + code));

        return mapToDtoWithComparison(today);
    }

    @Override
    @Transactional
    public void createCurrency(CurrencyDTO dto) {
        LocalDate today = LocalDate.now();
        if (repository.existsByCodeAndRateDate(dto.code(), today)) {
            throw new IllegalArgumentException("Currency rate already exists for " + dto.code() + " on " + today);
        }
        validateCurrencyDTO(dto);
        saveOrUpdateSingleCurrency(dto);
        log.info("Currency created successfully: {}", dto.code());
    }

    /**
     * Map Currency entity to DTO with yesterday comparison
     */
    private CurrencyDTO mapToDtoWithComparison(Currency today) {
        // Calculate today's T24 rates
        BigDecimal buyT24Today = safeAdd(today.getBuyrate(), today.getBuyspreadrate());
        BigDecimal sellT24Today = safeAdd(today.getSellrate(), today.getSellspreadrate());

        // Fetch yesterday's rate (or last available rate before today)
        Currency yesterday = repository.findPreviousRate(today.getCode(), today.getRateDate())
                .orElse(null);

        // Calculate yesterday's values if available
        BigDecimal prevBnr = yesterday != null ? yesterday.getBnrrate() : null;
        BigDecimal prevBuyT24 = yesterday != null
                ? safeAdd(yesterday.getBuyrate(), yesterday.getBuyspreadrate())
                : null;
        BigDecimal prevSellT24 = yesterday != null
                ? safeAdd(yesterday.getSellrate(), yesterday.getSellspreadrate())
                : null;

        return new CurrencyDTO(
                today.getCode(),
                today.getName(),
                today.getBnrrate(),
                today.getBuyrate(),
                today.getSellrate(),
                today.getBuyspreadrate(),
                today.getSellspreadrate(),
                buyT24Today,
                sellT24Today,
                prevBnr,
                calculateDiff(today.getBnrrate(), prevBnr),
                calculatePct(today.getBnrrate(), prevBnr),
                prevBuyT24,
                calculateDiff(buyT24Today, prevBuyT24),
                calculatePct(buyT24Today, prevBuyT24),
                prevSellT24,
                calculateDiff(sellT24Today, prevSellT24),
                calculatePct(sellT24Today, prevSellT24),
                yesterday != null
        );
    }

    /**
     * NULL-SAFE HELPERS
     */
    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        return safeValue(a).add(safeValue(b));
    }

    private BigDecimal calculateDiff(BigDecimal today, BigDecimal yesterday) {
        if (today == null || yesterday == null) {
            return null;
        }
        return today.subtract(yesterday);
    }

    private BigDecimal calculatePct(BigDecimal today, BigDecimal yesterday) {
        if (today == null || yesterday == null || yesterday.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return today.subtract(yesterday)
                .divide(yesterday, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
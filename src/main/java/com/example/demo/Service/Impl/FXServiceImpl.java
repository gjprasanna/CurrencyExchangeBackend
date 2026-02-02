



package com.example.demo.Service.Impl;

import com.example.demo.Dto.ConversionRequest;
import com.example.demo.Dto.ConversionResponse;
import com.example.demo.modal.Currency;
import com.example.demo.Repository.CurrencyRepository;
import com.example.demo.Service.FXService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class FXServiceImpl implements FXService {

    private static final Logger log = LoggerFactory.getLogger(FXServiceImpl.class);
    private final CurrencyRepository repository;

    public FXServiceImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public ConversionResponse convertCurrency(ConversionRequest request) {
        log.info("Converting {} {} to {}", request.getAmount(), request.getSellCode(), request.getBuyCode());

        // 1. Fetch LATEST currencies (today's rates)
        Currency buyCcy = repository.findLatestByCode(request.getBuyCode())
                .orElseThrow(() -> new IllegalArgumentException("Buy Currency not found: " + request.getBuyCode()));
        Currency sellCcy = repository.findLatestByCode(request.getSellCode())
                .orElseThrow(() -> new IllegalArgumentException("Sell Currency not found: " + request.getSellCode()));

        // 2. Validate Rates are not null or zero
        if (isInvalid(buyCcy.getBnrrate())) {
            throw new IllegalArgumentException("Buy currency BNR rate is missing or zero for " + request.getBuyCode());
        }
        if (isInvalid(sellCcy.getBnrrate())) {
            throw new IllegalArgumentException("Sell currency BNR rate is missing or zero for " + request.getSellCode());
        }

        // 3. Current T24 Rates (Today) - NULL-SAFE
        BigDecimal buyT24Today = safeAdd(buyCcy.getBuyrate(), buyCcy.getBuyspreadrate());
        BigDecimal sellT24Today = safeAdd(sellCcy.getSellrate(), sellCcy.getSellspreadrate());

        log.debug("Buy T24: {}, Sell T24: {}", buyT24Today, sellT24Today);

        // 4. Fetch YESTERDAY's rates for comparison
        Currency buyCcyYesterday = repository.findPreviousRate(request.getBuyCode(), buyCcy.getRateDate())
                .orElse(null);
        Currency sellCcyYesterday = repository.findPreviousRate(request.getSellCode(), sellCcy.getRateDate())
                .orElse(null);

        // 5. Magnitude-Based Treasury Rate (Today vs Yesterday)
        BigDecimal treasuryToday = calculateMagnitudeRate(buyCcy.getBnrrate(), sellCcy.getBnrrate());
        BigDecimal treasuryYesterday = null;

        if (buyCcyYesterday != null && sellCcyYesterday != null) {
            treasuryYesterday = calculateMagnitudeRate(
                    buyCcyYesterday.getBnrrate(),
                    sellCcyYesterday.getBnrrate()
            );
        }

        // 6. Magnitude-Based System T24 Rate (Today vs Yesterday)
        BigDecimal systemT24Today = calculateMagnitudeRate(buyT24Today, sellT24Today);
        BigDecimal systemT24Yesterday = null;

        if (buyCcyYesterday != null && sellCcyYesterday != null) {
            BigDecimal buyT24Yesterday = safeAdd(
                    buyCcyYesterday.getBuyrate(),
                    buyCcyYesterday.getBuyspreadrate()
            );
            BigDecimal sellT24Yesterday = safeAdd(
                    sellCcyYesterday.getSellrate(),
                    sellCcyYesterday.getSellspreadrate()
            );
            systemT24Yesterday = calculateMagnitudeRate(buyT24Yesterday, sellT24Yesterday);
        }

        // 7. Override Logic - use custom rate if provided and valid
        BigDecimal finalRate = (request.getCustomCustomerRate() != null
                && request.getCustomCustomerRate().compareTo(BigDecimal.ZERO) > 0)
                ? request.getCustomCustomerRate()
                : systemT24Today;

        log.debug("Final customer rate: {} (custom: {})", finalRate, request.getCustomCustomerRate() != null);

        // 8. Spread & P&L (Magnitude Based as per Org Rules)
        BigDecimal spread = calculateSpread(sellT24Today, buyT24Today, finalRate, treasuryToday);

        // **NEW**: 8a. Calculate Margin Value = Transaction Amount × Margin Spread
        BigDecimal marginValue = calculateMarginValue(request.getAmount(), spread);
        log.debug("Margin Value calculated: {} (Amount: {} × Spread: {})",
                marginValue, request.getAmount(), spread);

        // 9. Final Conversion Amount (Buy/Sell ratio)
        BigDecimal conversionRatio = buyCcy.getBnrrate()
                .divide(sellCcy.getBnrrate(), 10, RoundingMode.HALF_UP);
        BigDecimal finalAmount = request.getAmount()
                .multiply(conversionRatio)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Conversion completed: {} {} = {} {}",
                request.getAmount(), request.getSellCode(),
                finalAmount, request.getBuyCode());

        // 10. Build Response
        ConversionResponse response = new ConversionResponse();

        // --- BASIC FIELDS ---
        response.setBuyCode(request.getBuyCode());
        response.setSellCode(request.getSellCode());
        response.setInputAmount(request.getAmount());
        response.setConversionRate(conversionRatio.setScale(6, RoundingMode.HALF_UP));
        response.setFinalAmount(finalAmount);
        response.setBuyCustomerRate(buyT24Today);
        response.setSellCustomerRate(sellT24Today);

        // --- TREND: BUY SIDE BNR ---
        response.setBuyBnrToday(buyCcy.getBnrrate());
        response.setBuyBnrDiff(calculateDiff(
                buyCcy.getBnrrate(),
                buyCcyYesterday != null ? buyCcyYesterday.getBnrrate() : null
        ));
        response.setBuyBnrPct(calculatePct(
                buyCcy.getBnrrate(),
                buyCcyYesterday != null ? buyCcyYesterday.getBnrrate() : null
        ));

        // --- TREND: SELL SIDE BNR ---
        response.setSellBnrToday(sellCcy.getBnrrate());
        response.setSellBnrDiff(calculateDiff(
                sellCcy.getBnrrate(),
                sellCcyYesterday != null ? sellCcyYesterday.getBnrrate() : null
        ));
        response.setSellBnrPct(calculatePct(
                sellCcy.getBnrrate(),
                sellCcyYesterday != null ? sellCcyYesterday.getBnrrate() : null
        ));

        // --- TREND: TREASURY RATE ---
        response.setTreasuryRate(treasuryToday != null ? treasuryToday.setScale(6, RoundingMode.HALF_UP) : null);
        response.setTreasuryDiff(calculateDiff(treasuryToday, treasuryYesterday));
        response.setTreasuryPct(calculatePct(treasuryToday, treasuryYesterday));

        // --- TREND: CUSTOMER / T24 RATE ---
        response.setCustomerRate(finalRate.setScale(6, RoundingMode.HALF_UP));
        response.setCustomerDiff(calculateDiff(systemT24Today, systemT24Yesterday));
        response.setCustomerPct(calculatePct(systemT24Today, systemT24Yesterday));

        // --- SPREAD & PNL ---
        response.setSpread(spread.setScale(6, RoundingMode.HALF_UP));
        response.setPnlStatus(determinePnlStatus(spread));

        // **NEW**: Set Margin Value
        response.setMarginValue(marginValue);

        response.setTradingDate(buyCcy.getRateDate());

        return response;
    }

    /**
     * **NEW METHOD**: Calculate Margin Value
     * Formula: Transaction Amount × Margin Spread
     */
    private BigDecimal calculateMarginValue(BigDecimal transactionAmount, BigDecimal spread) {
        if (transactionAmount == null || spread == null) {
            log.warn("Cannot calculate margin value: transactionAmount or spread is null");
            return BigDecimal.ZERO;
        }

        return transactionAmount.multiply(spread).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * NULL-SAFE ADDITION
     */
    private BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        BigDecimal safeA = (a != null) ? a : BigDecimal.ZERO;
        BigDecimal safeB = (b != null) ? b : BigDecimal.ZERO;
        return safeA.add(safeB);
    }

    /**
     * Helper for Organization's "Larger / Smaller" Rule
     */
    private BigDecimal calculateMagnitudeRate(BigDecimal val1, BigDecimal val2) {
        if (val1 == null || val2 == null) {
            log.warn("Null value in magnitude calculation");
            return null;
        }

        if (val1.compareTo(BigDecimal.ZERO) == 0 || val2.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Zero value in magnitude calculation");
            return null;
        }

        if (val2.compareTo(val1) > 0) {
            return val2.divide(val1, 10, RoundingMode.HALF_UP);
        } else {
            return val1.divide(val2, 10, RoundingMode.HALF_UP);
        }
    }

    /**
     * Calculate spread based on magnitude comparison
     */
    private BigDecimal calculateSpread(BigDecimal sellT24, BigDecimal buyT24,
                                       BigDecimal finalRate, BigDecimal treasuryRate) {
        if (treasuryRate == null) {
            log.warn("Treasury rate is null, returning zero spread");
            return BigDecimal.ZERO;
        }

        if (sellT24.compareTo(buyT24) > 0) {
            return finalRate.subtract(treasuryRate);
        } else {
            return treasuryRate.subtract(finalRate);
        }
    }

    /**
     * Determine P&L status from spread
     */
    private String determinePnlStatus(BigDecimal spread) {
        int comparison = spread.signum();
        if (comparison > 0) {
            return "PROFIT";
        } else if (comparison < 0) {
            return "LOSS";
        } else {
            return "NEUTRAL";
        }
    }

    /**
     * Calculate difference between today and yesterday
     */
    private BigDecimal calculateDiff(BigDecimal today, BigDecimal yesterday) {
        if (today == null || yesterday == null) {
            return null;
        }
        return today.subtract(yesterday);
    }

    /**
     * Calculate percentage change
     */
    private BigDecimal calculatePct(BigDecimal today, BigDecimal yesterday) {
        if (today == null || yesterday == null || yesterday.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return today.subtract(yesterday)
                .divide(yesterday, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Check if rate is invalid (null or <= 0)
     */
    private boolean isInvalid(BigDecimal rate) {
        return rate == null || rate.compareTo(BigDecimal.ZERO) <= 0;
    }
}
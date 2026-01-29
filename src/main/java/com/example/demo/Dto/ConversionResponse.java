package com.example.demo.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Updated Response DTO for FX conversion result
 * Includes historical trend data (Difference and Percentage) for all components.
 */
public class ConversionResponse {
    // Basic Request Info
    private String buyCode;
    private String sellCode;
    private BigDecimal inputAmount;

    // Final Conversion Results
    private BigDecimal conversionRate; // (Buy BNR / Sell BNR)
    private BigDecimal finalAmount;   // (Input * conversionRate)

    // 1. BUY SIDE BNR TREND
    private BigDecimal buyBnrToday;
    private BigDecimal buyBnrDiff;    // (Today - Yesterday)
    private BigDecimal buyBnrPct;     // % Change

    // 2. SELL SIDE BNR TREND
    private BigDecimal sellBnrToday;
    private BigDecimal sellBnrDiff;
    private BigDecimal sellBnrPct;

    // 3. TREASURY RATE TREND (Magnitude Based: Larger/Smaller)
    private BigDecimal treasuryRate;  // Today's Treasury
    private BigDecimal treasuryDiff;
    private BigDecimal treasuryPct;

    // 4. CUSTOMER / T24 RATE TREND (Magnitude Based: Larger/Smaller)
    private BigDecimal customerRate;  // Final applied rate (System or Custom)
    private BigDecimal customerDiff;
    private BigDecimal customerPct;

    // Raw T24 values for individual currencies (Today)
    private BigDecimal buyCustomerRate;  // Buy T24 Today
    private BigDecimal sellCustomerRate; // Sell T24 Today

    // P&L and Spread
    private BigDecimal spread;
    private String pnlStatus;

    // **NEW FIELD**: Margin Value = Transaction Amount × Margin Spread
    private BigDecimal marginValue;

    private LocalDate tradingDate;

    // Constructors
    public ConversionResponse() {}

    // Getters and Setters

    public LocalDate getTradingDate() { return tradingDate; }
    public void setTradingDate(LocalDate tradingDate) { this.tradingDate = tradingDate; }

    public String getBuyCode() { return buyCode; }
    public void setBuyCode(String buyCode) { this.buyCode = buyCode; }

    public String getSellCode() { return sellCode; }
    public void setSellCode(String sellCode) { this.sellCode = sellCode; }

    public BigDecimal getInputAmount() { return inputAmount; }
    public void setInputAmount(BigDecimal inputAmount) { this.inputAmount = inputAmount; }

    public BigDecimal getConversionRate() { return conversionRate; }
    public void setConversionRate(BigDecimal conversionRate) { this.conversionRate = conversionRate; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public BigDecimal getBuyBnrToday() { return buyBnrToday; }
    public void setBuyBnrToday(BigDecimal buyBnrToday) { this.buyBnrToday = buyBnrToday; }

    public BigDecimal getBuyBnrDiff() { return buyBnrDiff; }
    public void setBuyBnrDiff(BigDecimal buyBnrDiff) { this.buyBnrDiff = buyBnrDiff; }

    public BigDecimal getBuyBnrPct() { return buyBnrPct; }
    public void setBuyBnrPct(BigDecimal buyBnrPct) { this.buyBnrPct = buyBnrPct; }

    public BigDecimal getSellBnrToday() { return sellBnrToday; }
    public void setSellBnrToday(BigDecimal sellBnrToday) { this.sellBnrToday = sellBnrToday; }

    public BigDecimal getSellBnrDiff() { return sellBnrDiff; }
    public void setSellBnrDiff(BigDecimal sellBnrDiff) { this.sellBnrDiff = sellBnrDiff; }

    public BigDecimal getSellBnrPct() { return sellBnrPct; }
    public void setSellBnrPct(BigDecimal sellBnrPct) { this.sellBnrPct = sellBnrPct; }

    public BigDecimal getTreasuryRate() { return treasuryRate; }
    public void setTreasuryRate(BigDecimal treasuryRate) { this.treasuryRate = treasuryRate; }

    public BigDecimal getTreasuryDiff() { return treasuryDiff; }
    public void setTreasuryDiff(BigDecimal treasuryDiff) { this.treasuryDiff = treasuryDiff; }

    public BigDecimal getTreasuryPct() { return treasuryPct; }
    public void setTreasuryPct(BigDecimal treasuryPct) { this.treasuryPct = treasuryPct; }

    public BigDecimal getCustomerRate() { return customerRate; }
    public void setCustomerRate(BigDecimal customerRate) { this.customerRate = customerRate; }

    public BigDecimal getCustomerDiff() { return customerDiff; }
    public void setCustomerDiff(BigDecimal customerDiff) { this.customerDiff = customerDiff; }

    public BigDecimal getCustomerPct() { return customerPct; }
    public void setCustomerPct(BigDecimal customerPct) { this.customerPct = customerPct; }

    public BigDecimal getBuyCustomerRate() { return buyCustomerRate; }
    public void setBuyCustomerRate(BigDecimal buyCustomerRate) { this.buyCustomerRate = buyCustomerRate; }

    public BigDecimal getSellCustomerRate() { return sellCustomerRate; }
    public void setSellCustomerRate(BigDecimal sellCustomerRate) { this.sellCustomerRate = sellCustomerRate; }

    public BigDecimal getSpread() { return spread; }
    public void setSpread(BigDecimal spread) { this.spread = spread; }

    public String getPnlStatus() { return pnlStatus; }
    public void setPnlStatus(String pnlStatus) { this.pnlStatus = pnlStatus; }

    public BigDecimal getMarginValue() { return marginValue; }
    public void setMarginValue(BigDecimal marginValue) { this.marginValue = marginValue; }
}




















//public record ConversionResponse(
//        String buyCode,
//        String sellCode,
//        BigDecimal inputAmount,
//        BigDecimal conversionRate,
//        BigDecimal finalAmount,
//        BigDecimal buyCustomerRate,  // Added
//        BigDecimal sellCustomerRate,  // Added
//        BigDecimal treasuryRate,  // NEW: Formula 1
//        BigDecimal customerRate ,   // NEW: Formula 2
//        BigDecimal spread,     // New field
//        String pnlStatus
//) {}
package com.example.demo.modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Archive table for currency rates older than 30 days
 * Same structure as Currency but for long-term storage
 */
@Entity
@Table(name = "currency_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code", "rate_date"}),
        indexes = {
                @Index(name = "idx_history_code_date", columnList = "code, rate_date DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column
    private LocalDateTime lastModified;

    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt; // When it was moved to history

    // DAILY RATES
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal bnrrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal buyrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal sellrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal buyspreadrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal sellspreadrate = BigDecimal.ZERO;

    // Helper method to calculate T24 rates
    public BigDecimal getBuyT24Rate() {
        return buyrate.add(buyspreadrate);
    }

    public BigDecimal getSellT24Rate() {
        return sellrate.add(sellspreadrate);
    }
}
package ru.redcarpet.stocks.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;

@Entity
@Table(name = "exchange_rates", schema = "stocks")
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(name = "base_currency", nullable = false, length = 3)
    private Currency baseCurrency;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(nullable = false, length = 32)
    private String source;   // CBR, BROKER_REPORT, MANUAL

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}
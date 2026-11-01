package ru.redcarpet.stocks.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.redcarpet.stocks.model.enums.CurrencyTradeOperation;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;

@Entity
@Table(name = "currency_trades", schema = "stocks")
@Getter
@Setter
@NoArgsConstructor
public class CurrencyTradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 32)
    private String instrument;

    @Column(name = "lot_currency", nullable = false, length = 3)
    private Currency lotCurrency;

    @Column(name = "counter_currency", nullable = false, length = 3)
    private Currency counterCurrency;

    @Column(name = "trade_number", nullable = false, length = 64)
    private String tradeNumber;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 16)
    private CurrencyTradeOperation operationType;   // BUY / SELL

    @Column(name = "rate", precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(name = "lot_volume", precision = 19, scale = 6)
    private BigDecimal lotVolume;

    @Column(name = "counter_volume", precision = 19, scale = 2)
    private BigDecimal counterVolume;

    @Column(length = 64)
    private String platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TradeSource source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }
}
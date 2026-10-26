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
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;

@Entity
@Table(name = "cash_operations")
@Getter
@Setter
@NoArgsConstructor
public class CashOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 32)
    private CashOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private CashFlowDirection direction;

    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "target_currency", length = 3)
    private Currency targetCurrency;

    @Column(name = "target_amount", precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(length = 64)
    private String platform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TradeSource source;

    @Column(name = "dividend_issuer", length = 200)
    private String dividendIssuer;

    @Column(name = "dividend_isin", length = 12)
    private String dividendIsin;

    @Column(name = "dividend_record_date")
    private LocalDate dividendRecordDate;

    @Column(name = "dividend_tax", length = 64)
    private String dividendTax;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

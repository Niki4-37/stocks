package ru.redcarpet.stocks.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import ru.redcarpet.stocks.model.exception.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;

@Entity
@Table(name = "holdings")
@Getter
@Setter
@NoArgsConstructor
public class Holding {

    private static final int AVG_PRICE_SCALE = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "security_id", nullable = false)
    private Security security;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "avg_price", nullable = false, precision = 19, scale = 6)
    private BigDecimal avgPrice = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "total_dividends", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDividends = BigDecimal.ZERO;

    @Column(name = "total_coupons", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCoupons = BigDecimal.ZERO;

    @Column(name = "realized_pnl", nullable = false, precision = 19, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;

    @Column(name = "first_buy_date")
    private LocalDate firstBuyDate;

    @Column(name = "last_event_date")
    private LocalDate lastEventDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ====== actions
    public void applyEvent(Event event) {
        switch (event.getOperationType()) {
            case BUY, TRANSFER_IN   -> applyBuy(event);
            case SELL, TRANSFER_OUT -> applySell(event);
            default -> throw new ValidationException(
                    "Unsupported operation: " + event.getOperationType());
        }
    }

    public void applyBuy(Event event) {
        BigDecimal buyQty = event.getQuantity();
        BigDecimal buyPrice = event.getPrice();

        BigDecimal oldQty = this.quantity;
        BigDecimal newQty = oldQty.add(buyQty);

        if (oldQty.signum() == 0) {
            this.avgPrice = buyPrice;
        } else {
            this.avgPrice = oldQty.multiply(this.avgPrice)
                    .add(buyQty.multiply(buyPrice))
                    .divide(newQty, AVG_PRICE_SCALE, RoundingMode.HALF_UP);
        }

        this.quantity = newQty;
        updateLastEventDate(event.getEventDate());
        updateFirstBuyDate(event.getEventDate());
    }

    public void applySell(Event event) {
        BigDecimal sellQty = event.getQuantity();
        if (this.quantity.compareTo(sellQty) < 0) {
            throw new ValidationException(String.format(
                    "Cannot sell %s of %s: only %s held",
                    sellQty, this.security.getTicker(), this.quantity
            ));
        }

        BigDecimal pnl = event.getPrice()
                .subtract(this.avgPrice)
                .multiply(sellQty)
                .setScale(2, RoundingMode.HALF_UP);

        this.realizedPnl = this.realizedPnl.add(pnl);

        this.quantity = this.quantity.subtract(sellQty);
        updateLastEventDate(event.getEventDate());
    }

    public void applyDividend(BigDecimal amount, Currency currency, LocalDate date) {
        requireSameCurrency(currency);
        this.totalDividends = this.totalDividends.add(amount);
        updateLastEventDate(date);
    }

    public void applyCoupon(BigDecimal amount, Currency currency, LocalDate date) {
        requireSameCurrency(currency);
        this.totalCoupons = this.totalCoupons.add(amount);
        updateLastEventDate(date);
    }

    // ====== utils
    private void requireSameCurrency(Currency eventCurrency) {
        if (eventCurrency.equals(this.currency)) return;
        throw new ValidationException(String.format(
                "Dividend/coupon in %s differs from position currency %s — not supported yet",
                eventCurrency, this.currency));
    }

    private void updateLastEventDate(LocalDate eventDate) {
        if (this.lastEventDate == null || eventDate.isAfter(this.lastEventDate)) {
            this.lastEventDate = eventDate;
        }
    }

    private void updateFirstBuyDate(LocalDate date) {
        if (this.firstBuyDate == null || date.isBefore(this.firstBuyDate)) {
            this.firstBuyDate = date;
        }
    }

    public BigDecimal getCostBasis() {
        return quantity.multiply(avgPrice);
    }

    // ====== lifecycle
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

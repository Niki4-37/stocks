package ru.redcarpet.stocks.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.redcarpet.stocks.model.enums.CurrencyTradeOperation;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;

public record CreateCurrencyExchangeRequest(
        @NotNull Long portfolioId,
        @NotNull LocalDate operationDate,

        @NotNull Currency lotCurrency,
        @NotNull Currency counterCurrency,
        @NotNull CurrencyTradeOperation operationType,

        @Positive BigDecimal lotVolume,
        @Positive BigDecimal counterVolume,
        @Positive BigDecimal rate,

        String comment,

        // ---- внешний ключ сделки (для импорта из отчёта) ----
        String tradeNumber,
        String platform,

        // ---- источник: MANUAL (по умолчанию) / BROKER_REPORT_BKS ----
        TradeSource source
) {
    public boolean isManual() { return source == TradeSource.MANUAL; }
    public boolean isBuying() { return operationType == CurrencyTradeOperation.BUY; }

    public BigDecimal resolvedRate() {
        if (rate != null) return rate;
        if (counterVolume != null && lotVolume != null) {
            return counterVolume.divide(lotVolume, 8, RoundingMode.HALF_UP);
        }
        throw new ValidationException(
                "Cannot resolve rate: need rate or (lotVolume + counterVolume)");
    }

    public BigDecimal resolvedLotVolume() {
        if (lotVolume != null) return lotVolume;
        if (counterVolume != null) {
            return counterVolume.divide(resolvedRate(), 6, RoundingMode.HALF_UP);
        }
        throw new ValidationException(
                "Cannot resolve lotVolume: need lotVolume or (counterVolume + rate)");
    }

    public BigDecimal resolvedCounterVolume() {
        if (counterVolume != null) return counterVolume;
        if (lotVolume != null) {
            return lotVolume.multiply(resolvedRate()).setScale(2, RoundingMode.HALF_UP);
        }
        throw new ValidationException(
                "Cannot resolve counterVolume: need counterVolume or (lotVolume + rate)");
    }

    /** Валидация: минимум два из трёх (lotVolume/counterVolume/rate), lot != counter. */
    public void validate() {
        if (lotCurrency == null || counterCurrency == null) {
            throw new ValidationException("lotCurrency and counterCurrency are required");
        }
        if (lotCurrency.equals(counterCurrency)) {
            throw new ValidationException("lot and counter currency must differ");
        }
        if (operationType == null) {
            throw new ValidationException("operationType is required");
        }
        int provided = (lotVolume != null ? 1 : 0)
                + (counterVolume != null ? 1 : 0)
                + (rate != null ? 1 : 0);
        if (provided < 2) {
            throw new ValidationException(
                    "At least two of lotVolume/counterVolume/rate must be specified");
        }
    }
}

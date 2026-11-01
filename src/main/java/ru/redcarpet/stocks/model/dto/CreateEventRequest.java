package ru.redcarpet.stocks.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import ru.redcarpet.stocks.model.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

@Getter
public class CreateEventRequest {

    @NotNull
    private Long portfolioId;

    private String ticker;

    @NotNull
    private OperationType operationType;

    @NotNull
    private LocalDate eventDate;

    /** Цена за одну бумагу (или сумма для DIVIDEND/COUPON/FEE) */
    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @NotNull
    private Currency currency;

    /**
     * Курс валюты к основной валюте портфеля на момент сделки.
     * null, если currency == основная валюта.
     */
    private BigDecimal exchangeRate;

    /** Количество ценных бумаг. null для денежных операций */
    private BigDecimal quantity;

    public CreateEventRequest() {}

    @JsonCreator
    public CreateEventRequest( @JsonProperty("portfolioId") Long portfolioId,
                               @JsonProperty("ticker") String ticker,
                               @JsonProperty("operationType") OperationType operationType,
                               @JsonProperty("eventDate") LocalDate eventDate,
                               @JsonProperty("price") BigDecimal price,
                               @JsonProperty("currency") Currency currency,
                               @JsonProperty("rate") BigDecimal exchangeRate,
                               @JsonProperty("quantity") BigDecimal quantity
    ) {
        this.portfolioId = portfolioId;
        this.ticker = ticker;
        this.operationType = operationType;
        this.eventDate = eventDate;
        this.price = price;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.quantity = quantity;
    }
}

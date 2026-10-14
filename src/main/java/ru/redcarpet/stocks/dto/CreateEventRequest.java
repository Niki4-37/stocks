package ru.redcarpet.stocks.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import ru.redcarpet.stocks.enums.OperationType;
import ru.redcarpet.stocks.enums.SecurityType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

@Getter
public class CreateEventRequest {

    private SecurityType securityType;

    @NotNull
    private String secId;

    private OperationType operationType;

    @NotNull
    private LocalDate eventDate;

    /** Цена за одну бумагу (или сумма для DIVIDEND/COUPON/FEE) */
    @NotNull
    private BigDecimal price;

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
    public CreateEventRequest( @JsonProperty("securityType") SecurityType securityType,
                               @JsonProperty("secId") String secId,
                               @JsonProperty("operationType") OperationType operationType,
                               @JsonProperty("eventDate") LocalDate eventDate,
                               @JsonProperty("price") BigDecimal price,
                               @JsonProperty("currency") Currency currency,
                               @JsonProperty("exchangeRate") BigDecimal exchangeRate,
                               @JsonProperty("quantity") BigDecimal quantity
    ) {
        this.securityType = securityType;
        this.secId = secId;
        this.operationType = operationType;
        this.eventDate = eventDate;
        this.price = price;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.quantity = quantity;
    }
}

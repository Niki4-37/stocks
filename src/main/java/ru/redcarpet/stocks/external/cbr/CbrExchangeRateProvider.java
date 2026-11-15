package ru.redcarpet.stocks.external.cbr;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.exchange.ExchangeRateProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CbrExchangeRateProvider implements ExchangeRateProvider {

    private final CbrParsedClient client;

    @Override
    public Optional<BigDecimal> getRate(Currency currency, Currency baseCurrency, LocalDate date) {
        // ЦБ даёт RUB → валюта; для base ≠ RUB — кросс через RUB
        if (currency.equals(baseCurrency)) return Optional.of(BigDecimal.ONE);

        Map<Currency, BigDecimal> ratesToRub = client.getRatesOnDate(date);

        BigDecimal fromRub = ratesToRub.get(currency);
        BigDecimal toRub   = ratesToRub.get(baseCurrency);

        if (fromRub == null || toRub == null) return Optional.empty();
        return Optional.of(fromRub.divide(toRub, 8, RoundingMode.HALF_UP));
    }
}
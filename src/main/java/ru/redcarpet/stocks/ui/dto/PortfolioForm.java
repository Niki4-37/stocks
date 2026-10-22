package ru.redcarpet.stocks.ui.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.redcarpet.stocks.model.dto.CreatePortfolioRequest;

import java.math.BigDecimal;
import java.util.Currency;

@Setter
@Getter
@NoArgsConstructor
public class PortfolioForm {
    private String name;
    private Currency baseCurrency;
    private BigDecimal initialBalance;

    public CreatePortfolioRequest toRequest() {
        return new CreatePortfolioRequest(name, baseCurrency, initialBalance);
    }
}

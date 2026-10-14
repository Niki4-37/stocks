package ru.redcarpet.stocks.servicies.impl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.servicies.PortfolioService;

import java.util.Currency;

@Getter
@Setter
@Service
public class PortfolioServiceImpl implements PortfolioService {

    private Currency baseCurrency;
}

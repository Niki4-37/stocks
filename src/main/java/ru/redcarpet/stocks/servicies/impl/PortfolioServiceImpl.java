package ru.redcarpet.stocks.servicies.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.redcarpet.stocks.dto.BalanceResponse;
import ru.redcarpet.stocks.repositories.PortfolioBalanceRepository;
import ru.redcarpet.stocks.servicies.PortfolioService;

import java.util.Currency;
import java.util.List;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private Currency baseCurrency;

    private final PortfolioBalanceRepository balanceRepository;

    @Override
    public List<BalanceResponse> getBalances(Long id) {
        return balanceRepository.findAllByPortfolioId(id).stream()
                .map(pb -> new BalanceResponse(pb.getCurrency(), pb.getAmount()))
                .toList();
    }
}

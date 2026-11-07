package ru.redcarpet.stocks.verification.cash;

import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.cashflow.CashParseResult;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.util.List;
import java.util.stream.Stream;

@Component
public class CashCheckRequestMapper {

    public CashCheckRequest toCheckRequest(
            Long portfolioId,
            CashParseResult cashFlows,
            TradeSource source
    ) {
        List<CashKeyDto> keys = Stream.of(
                cashFlows.incomes(),
                        cashFlows.expenses(),
                        cashFlows.overnightInterests())
                .flatMap(List::stream)
                .map(this::toKey)
                .toList();

        return new CashCheckRequest(portfolioId, source, keys);
    }

    private CashKeyDto toKey(CashFlowRow cashFlowRow) {
        return new CashKeyDto(
                DateParser.parseBrokerDate(cashFlowRow.date()),
                cashFlowRow.operation(),
                cashFlowRow.platform(),
                cashFlowRow.direction(),
                cashFlowRow.amount(),
                cashFlowRow.currency()
        );
    }
}
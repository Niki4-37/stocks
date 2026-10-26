package ru.redcarpet.stocks.verification.cash;

import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.reports.cashflow.CashParseResult;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.util.ArrayList;
import java.util.List;

@Component
public class CashCheckRequestMapper {

    public CashCheckRequest toCheckRequest(
            Long portfolioId,
            CashParseResult cashFlows,
            TradeSource source
    ) {
        List<CashKeyDto> keys = new ArrayList<>(
                cashFlows.incomes().size()
                        + cashFlows.expenses().size()
                        + cashFlows.overnightInterests().size()
        );
        addKeys(keys, cashFlows.incomes());
        addKeys(keys, cashFlows.expenses());
        addKeys(keys, cashFlows.overnightInterests());

        return new CashCheckRequest(portfolioId, source, keys);
    }

    private static void addKeys(List<CashKeyDto> keys, List<CashFlowRow> rows) {
        for (CashFlowRow r : rows) {
            keys.add(toKey(r));
        }
    }

    private static CashKeyDto toKey(CashFlowRow cashFlowRow) {
        CashOperationType type = CashOperationType.byDisplayName(cashFlowRow.operation())
                .orElseThrow(() -> new ValidationException(
                        "Unknown operation: " + cashFlowRow.operation()));

        return new CashKeyDto(
                DateParser.parseBrokerDate(cashFlowRow.date()),
                type,
                cashFlowRow.platform(),
                cashFlowRow.direction(),
                DecimalParser.parse(cashFlowRow.amount()),
                cashFlowRow.currency()
        );
    }
}
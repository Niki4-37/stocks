package ru.redcarpet.stocks.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.dto.CreateCashOperationCommand;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.service.CashOperationService;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashImportService {

    private final CashOperationService cashOperationService;

    /**
     * Импортирует строки 1.1 в cash_operations.
     * Дедупликация по содержимому: если в отчёте N одинаковых строк,
     * а в БД уже M — создаём max(0, N-M).
     */
    @Transactional
    public int importFlows(Long portfolioId,
                           List<CashFlowRow> rows,
                           TradeSource source
    ) {
        if (rows.isEmpty()) return 0;

        // группируем отчётные строки по контентному ключу
        Map<String, List<CashFlowRow>> byKey = rows.stream()
                .collect(Collectors.groupingBy(
                        CashImportService::contentKey,
                        LinkedHashMap::new,
                        Collectors.toList()));

        int inserted = 0;

        for (List<CashFlowRow> sameRows : byKey.values()) {
            CashFlowRow first = sameRows.get(0);

            CashOperationType type = CashOperationType.byDisplayName(first.operation())
                    .orElseThrow(() -> new ValidationException(
                            "Unknown operation: " + first.operation()));

            LocalDate date = DateParser.parseBrokerDate(first.date());
            BigDecimal amount = DecimalParser.parse(first.amount());

            long existing = cashOperationService.count(
                    portfolioId, source, date, type,
                    first.platform(), first.direction(), amount, first.currency());

            long toInsert = sameRows.size() - existing;
            for (int i = 0; i < toInsert; i++) {
                cashOperationService.create(toCommand(portfolioId, first, type, date, amount, source));
                inserted++;
            }
        }
        return inserted;
    }

    private CreateCashOperationCommand toCommand(Long portfolioId, CashFlowRow row,
                                                 CashOperationType type, LocalDate date,
                                                 BigDecimal amount, TradeSource source) {
        CreateCashOperationCommand.DividendFields dividend = null;
        if (type.isDividend() && row.dividendNote() != null) {
            dividend = new CreateCashOperationCommand.DividendFields(
                    row.dividendNote().issuer(),
                    row.dividendNote().isin(),
                    DateParser.parseBrokerDateOrNull(row.dividendNote().recordDate()),
                    row.dividendNote().tax()
            );
        }

        return new CreateCashOperationCommand(
                portfolioId,
                type,
                row.direction(),
                date,
                row.currency(),
                amount,
                row.platform(),
                source,
                row.noteRaw(),
                dividend
        );
    }

    private static String contentKey(CashFlowRow r) {
        return r.date() + "|" + r.operation() + "|" + r.platform() + "|" + r.direction() + "|" + r.amount();
    }
}

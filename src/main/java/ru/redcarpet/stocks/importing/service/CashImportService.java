package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.importing.dto.CashImportResult;
import ru.redcarpet.stocks.importing.dto.ImportResult;
import ru.redcarpet.stocks.model.dto.CreateCashOperationCommand;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.service.CashOperationService;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
    public CashImportResult importFlows(Long portfolioId,
                                        List<CashFlowRow> rows,
                                        TradeSource source
    ) {
        if (rows.isEmpty()) return CashImportResult.empty();

        // группируем отчётные строки по контентному ключу
        Map<String, List<CashFlowRow>> byKey = rows.stream()
                .collect(Collectors.groupingBy(
                        CashImportService::contentKey,
                        LinkedHashMap::new,
                        Collectors.toList()));

        int inserted = 0;
        int skipped = 0;
        List<String> warnings = new ArrayList<>();
        List<CashOperation> savedDividends = new ArrayList<>();

        for (List<CashFlowRow> sameRows : byKey.values()) {
            CashFlowRow first = sameRows.get(0);

            CashOperationType type = first.operation();

            LocalDate date;
            try {
                date = DateParser.parseBrokerDate(first.date());
            } catch (RuntimeException e) {
                warnings.add("Bad row: " + first + " (" + e.getMessage() + ")");
                skipped += sameRows.size();
                continue;
            }

            long existing = cashOperationService.count(
                    portfolioId, source, date, type,
                    first.platform(), first.direction(), first.amount(), first.currency());

            long toInsert = sameRows.size() - existing;
            if (toInsert <= 0) {
                skipped += sameRows.size();
                continue;
            }

            for (int i = 0; i < toInsert; i++) {
                log.info("Importing cash operation: operation={}, date={}, direction={}, amount={}",
                        first.operation(), first.date(), first.direction(), first.amount());
                CashOperation saved = cashOperationService.create(toCommand(portfolioId, first, date, source));
                inserted++;

                if (first.operation().hasDividendFeature()) {
                    savedDividends.add(saved);
                }
            }
            skipped += sameRows.size() - toInsert;
        }

        return new CashImportResult(
                new ImportResult(inserted, skipped, warnings),
                savedDividends);
    }

    private CreateCashOperationCommand toCommand(Long portfolioId, CashFlowRow row,
                                                 LocalDate date, TradeSource source
    ) {
        CreateCashOperationCommand.DividendFields dividend = null;
        if (row.operation().hasDividendFeature() && row.dividendNote() != null) {
            dividend = new CreateCashOperationCommand.DividendFields(
                    row.dividendNote().issuer(),
                    row.dividendNote().isin(),
                    DateParser.parseBrokerDateOrNull(row.dividendNote().recordDate()),
                    row.dividendNote().tax()
            );
        }

        return new CreateCashOperationCommand(
                portfolioId,
                row.operation(),
                row.direction(),
                date,
                row.currency(),
                row.amount(),
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

package ru.redcarpet.stocks.importing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.CashOperationRepository;
import ru.redcarpet.stocks.model.repositories.PortfolioRepository;
import ru.redcarpet.stocks.reports.common.DateParser;
import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;
import ru.redcarpet.stocks.verification.trades.TradeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashImportService {

    private final CashOperationRepository repository;
    private final PortfolioRepository portfolioRepository;

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
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found: " + portfolioId));

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

            CashFlowDirection direction = first.direction();

            LocalDate date = DateParser.parseBrokerDate(first.date());
            BigDecimal amount = DecimalParser.parse(first.amount());

            long existing = repository
                    .countByPortfolioIdAndSourceAndOperationDateAndOperationTypeAndPlatformAndDirectionAndAmountAndCurrency(
                            portfolioId, source, date, type, first.platform(), direction, amount, first.currency());

            long toInsert = sameRows.size() - existing;
            for (int i = 0; i < toInsert; i++) {
                repository.save(build(portfolio, first, type, direction, date, amount, source));
                inserted++;
            }
        }
        return inserted;
    }

    private CashOperation build(Portfolio portfolio, CashFlowRow row,
                                CashOperationType type, CashFlowDirection direction,
                                LocalDate date, BigDecimal amount,
                                TradeSource source) {
        CashOperation op = new CashOperation();
        op.setPortfolio(portfolio);
        op.setOperationType(type);
        op.setDirection(direction);
        op.setOperationDate(date);
        op.setCurrency(row.currency());
        op.setAmount(amount);
        op.setPlatform(row.platform());
        op.setSource(source);
        op.setComment(row.noteRaw());

        if (type.isDividend() && row.dividendNote() != null) {
            op.setDividendIssuer(row.dividendNote().issuer());
            op.setDividendIsin(row.dividendNote().isin());
            op.setDividendRecordDate(DateParser.parseBrokerDateOrNull(row.dividendNote().recordDate()));
            op.setDividendTax(row.dividendNote().tax());
        }
        return op;
    }

    private static String contentKey(CashFlowRow r) {
        return r.date() + "|" + r.operation() + "|" + r.platform() + "|" + r.direction() + "|" + r.amount();
    }
}

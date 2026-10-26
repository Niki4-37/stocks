package ru.redcarpet.stocks.verification.cash;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.repositories.CashOperationRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CashVerificationService {

    private final CashOperationRepository repository;

    @Transactional(readOnly = true)
    public CashCheckResponse check(CashCheckRequest request) {
        LocalDate minDate = request.operations().stream()
                .map(CashKeyDto::operationDate).min(LocalDate::compareTo).orElseThrow();
        LocalDate maxDate = request.operations().stream()
                .map(CashKeyDto::operationDate).max(LocalDate::compareTo).orElseThrow();

        List<CashOperation> existing = repository
                .findByPortfolioIdAndSourceAndOperationDateBetween(
                        request.portfolioId(), request.source(), minDate, maxDate);

        // сколько таких уже есть в БД
        Map<String, Long> remaining = new HashMap<>();
        for (CashOperation c : existing) {
            remaining.merge(contentKey(c), 1L, Long::sum);
        }

        List<CashCheckResult> results = new ArrayList<>();
        int matched = 0, missing = 0;

        for (CashKeyDto key : request.operations()) {
            String k = contentKey(key);
            long left = remaining.getOrDefault(k, 0L);

            if (left > 0) {
                remaining.put(k, left - 1);
                results.add(new CashCheckResult(key, CashMatchStatus.MATCHED));
                matched++;
            } else {
                results.add(new CashCheckResult(key, CashMatchStatus.MISSING));
                missing++;
            }
        }

        return new CashCheckResponse(request.portfolioId(), results,
                new Summary(results.size(), matched, missing));
    }

    private static String contentKey(CashOperation c) {
        return c.getOperationDate() + "|" + c.getOperationType() + "|"
                + c.getPlatform() + "|" + c.getDirection() + "|" + c.getAmount() + "|" + c.getCurrency();
    }

    private static String contentKey(CashKeyDto k) {
        return k.operationDate() + "|" + k.operationType() + "|"
                + k.platform() + "|" + k.direction() + "|" + k.amount() + "|" + k.currency();
    }
}

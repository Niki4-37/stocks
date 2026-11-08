package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.importing.dto.ImportResult;
import ru.redcarpet.stocks.model.entity.CashOperation;
import ru.redcarpet.stocks.model.entity.Portfolio;
import ru.redcarpet.stocks.model.entity.Security;
import ru.redcarpet.stocks.model.enums.CashOperationType;
import ru.redcarpet.stocks.model.repositories.SecurityRepository;
import ru.redcarpet.stocks.model.service.HoldingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateHoldingsService {

    private final SecurityRepository securityRepository;
    private final HoldingService holdingService;

    /**
     * Применяет сохранённые дивиденды/купоны к Holding.
     * Вызывать после импорта сделок (Holding уже создан из BUY/SELL).
     */
    @Transactional
    public ImportResult applySavedDividends(
            Portfolio portfolio,
            List<CashOperation> savedDividends
    ) {
        int applied = 0, skipped = 0;
        List<String> warnings = new ArrayList<>();

        for (CashOperation operation : savedDividends) {
            try {
                if (applyOne(portfolio, operation)) applied++; else skipped++;
            } catch (RuntimeException e) {
                log.warn("Failed to apply {} {}: {}",
                        operation.getOperationType(),
                        operation.getOperationDate(),
                        e.getMessage(), e);
                warnings.add(operation.getOperationType() + " " + operation.getOperationDate() + ": " + e.getMessage());
                skipped++;
            }
        }
        return new ImportResult(applied, skipped, warnings);
    }

    private boolean applyOne(Portfolio portfolio, CashOperation operation) {
        Optional<Security> secOpt = resolveSecurity(operation);
        if (secOpt.isEmpty()) return false;
        Security security = secOpt.get();

        if (operation.getOperationType() == CashOperationType.DIVIDEND) {
            holdingService.applyDividend(
                    portfolio.getId(), security.getId(),
                    operation.getAmount(), operation.getCurrency(), operation.getOperationDate());
        } else if (operation.getOperationType() == CashOperationType.COUPON) {
            holdingService.applyCoupon(
                    portfolio.getId(), security.getId(),
                    operation.getAmount(), operation.getCurrency(), operation.getOperationDate());
        } else {
            return false;
        }
        return true;
    }

    private Optional<Security> resolveSecurity(CashOperation operation) {
        if (operation == null) return Optional.empty();

        // 1) ISIN (точный)
        String isin = operation.getDividendIsin();
        if (isin != null && !isin.isBlank()) {
            Optional<Security> byIsin = securityRepository.findByIsin(isin);
            if (byIsin.isPresent()) return byIsin;
        }

        // 2) имя эмитента (точное без учёта регистра)
        String issuer = operation.getDividendIssuer();
        if (issuer == null || issuer.isBlank()) return Optional.empty();

        String normalized = normalize(issuer);
        List<Security> byName = securityRepository.findByNormalizedName(normalized);
        if (byName.isEmpty()) {
            log.warn("Security not found for dividend: isin={}, issuer={}", isin, issuer);
            return Optional.empty();
        }
        if (byName.size() > 1) {
            log.warn("Multiple securities for issuer '{}': {} — not applying",
                    issuer, byName.size());
            return Optional.empty();   // неоднозначность — не угадываем
        }
        return Optional.of(byName.get(0));
    }

    private String normalize(String name) {
        if (name == null) return null;
        return name.trim()
                .toLowerCase()
                .replaceAll("\\b(inc|corp|corporation|llc|ltd|plc|co)\\b\\.?", "")
                .replaceAll("[.,]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
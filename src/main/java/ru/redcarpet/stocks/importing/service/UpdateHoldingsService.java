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
import ru.redcarpet.stocks.model.exception.NotFoundException;
import ru.redcarpet.stocks.model.repositories.SecurityRepository;
import ru.redcarpet.stocks.model.service.HoldingService;

import java.util.ArrayList;
import java.util.List;

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
        String isin = operation.getDividendIsin();
        if (isin == null || isin.isBlank()) {
            return false;   // нет ISIN — некуда применить
        }

        Security security = securityRepository.findByIsin(isin).orElse(null);
        if (security == null) {
            throw new NotFoundException("Security not found for ISIN " + isin);
        }

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
}
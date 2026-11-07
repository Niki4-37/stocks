package ru.redcarpet.stocks.reports.cashflow;

import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.model.enums.CashFlowDirection;
import ru.redcarpet.stocks.reports.dto.CheckRow;
import ru.redcarpet.stocks.reports.dto.CashFlowRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Сверка детальных операций 1.1 с итогами 1.3. */
@Component
public final class FeesChecksBuilder {

    public List<CheckRow> build(
            List<CashFlowRow> operations,
            Map<String, BigDecimal> totalFees
    ) {
        Map<String, BigDecimal> detailSums = new LinkedHashMap<>();

        for (CashFlowRow cashFlowRow : operations) {
            if (!cashFlowRow.operation().hasCheckingFeature()) continue;
            BigDecimal value = cashFlowRow.direction() == CashFlowDirection.EXPENSE
                    ? cashFlowRow.amount()
                    : cashFlowRow.amount().negate();
            detailSums.merge(
                    key(cashFlowRow.operation().getDisplayName(), cashFlowRow.platform()),
                    value,
                    BigDecimal::add);
        }

        List<CheckRow> checks = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> fee : totalFees.entrySet()) {
            String[] parts = splitKey(fee.getKey());
            BigDecimal detail = detailSums.getOrDefault(fee.getKey(), BigDecimal.ZERO);
            checks.add(new CheckRow(parts[0], parts[1], detail, fee.getValue(),
                    detail.compareTo(fee.getValue()) == 0));
        }
        for (Map.Entry<String, BigDecimal> sum : detailSums.entrySet()) {
            if (totalFees.containsKey(sum.getKey())) continue;
            String[] parts = splitKey(sum.getKey());
            checks.add(new CheckRow(parts[0], parts[1],
                    sum.getValue(), BigDecimal.ZERO, false));
        }
        return checks;
    }

    private static String key(String op, String platform) { return op + "|" + platform; }
    private static String[] splitKey(String key) { return key.split("\\|", 2); }
}

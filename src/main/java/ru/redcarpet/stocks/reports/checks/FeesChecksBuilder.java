package ru.redcarpet.stocks.reports.checks;

import org.springframework.stereotype.Component;
import ru.redcarpet.stocks.reports.common.DecimalParser;
import ru.redcarpet.stocks.reports.dto.CheckRow;
import ru.redcarpet.stocks.reports.dto.OperationRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Сверка детальных операций 1.1 с итогами 1.3. */
@Component
public final class FeesChecksBuilder {

    public List<CheckRow> build(
            List<OperationRow> operations,
            Map<String, BigDecimal> totalFees
    ) {
        Map<String, BigDecimal> detailSums = new LinkedHashMap<>();
        for (OperationRow operationRow : operations) {
            BigDecimal parsedAmount = DecimalParser.parse(operationRow.amount());
            if (Objects.isNull(parsedAmount)) continue;
            detailSums.merge(
                    key(operationRow.operation(), operationRow.platform()),
                    parsedAmount,
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

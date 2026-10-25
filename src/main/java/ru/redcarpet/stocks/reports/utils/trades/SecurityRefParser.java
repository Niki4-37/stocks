package ru.redcarpet.stocks.reports.utils.trades;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.SecurityRef;

/** Извлекает SecurityRef из строки-идентификатора бумаги в разделе 2.1. */
// !!!Здесь неправильно определяется название ценной бумаги!!!
public final class SecurityRefParser {

    private static final String ISIN_LABEL = "ISIN:";
    private static final String REG_LABEL  = "Номер рег.:";

    private SecurityRefParser() {}

    /**
     * Пытается распознать строку-идентификатор бумаги.
     * @return SecurityRef или null, если строка не похожа на идентификатор бумаги
     */
    public static SecurityRef tryParse(Row row, DataFormatter fmt) {
        if (row == null) return null;

        String ticker = firstNonBlank(row, fmt);
        if (ticker.isBlank()) return null;

        // Должна быть хотя бы одна метка ISIN: / Номер рег.:
        String isin      = valueAfterLabel(row, fmt, ISIN_LABEL);
        String regNumber = valueAfterLabel(row, fmt, REG_LABEL);
        if (isin.isBlank() && regNumber.isBlank()) return null;

        String name = guessName(row, fmt);
        return new SecurityRef(ticker, isin, regNumber, name);
    }

    /** Первое непустое значение в строке (обычно столбец A = тикер). */
    private static String firstNonBlank(Row row, DataFormatter fmt) {
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (!v.isEmpty()) return v;
        }
        return "";
    }

    /** Значение из ячейки, следующей за ячейкой с меткой. */
    private static String valueAfterLabel(Row row, DataFormatter fmt, String label) {
        boolean found = false;
        for (Cell c : row) {
            if (found) {
                String v = fmt.formatCellValue(c).trim();
                if (!v.isEmpty()) return v;
                // если пусто — идём дальше, значение может быть через пропуск
                continue;
            }
            if (fmt.formatCellValue(c).trim().equalsIgnoreCase(label)) {
                found = true;
            }
        }
        return "";
    }

    /**
     * Название бумаги/эмитента — это длинная текстовая ячейка после ISIN/Номер рег.,
     * которая не совпадает ни с одной меткой и не является тикером. Берём первую такую.
     */
    private static String guessName(Row row, DataFormatter fmt) {
        String ticker = firstNonBlank(row, fmt);
        boolean afterIsin = false;
        for (Cell c : row) {
            String v = fmt.formatCellValue(c).trim();
            if (v.equalsIgnoreCase(ISIN_LABEL) || v.equalsIgnoreCase(REG_LABEL)) {
                afterIsin = true;
                continue;
            }
            if (!afterIsin) continue;
            if (v.isEmpty() || v.equals(ticker)) continue;
            // эвристика: "название" — длина > 3 и не совпадает с ISIN-подобным кодом
            if (v.length() > 3 && !v.matches("[A-Z]{2}[A-Z0-9]{9,10}")) {
                return v;
            }
            afterIsin = false; // прошли значение ISIN/рег.номера — сбрасываем
        }
        return "";
    }
}
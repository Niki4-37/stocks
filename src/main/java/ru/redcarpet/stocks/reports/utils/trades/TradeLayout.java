package ru.redcarpet.stocks.reports.utils.trades;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import ru.redcarpet.stocks.reports.dto.SecurityRef;
import ru.redcarpet.stocks.reports.enums.TradeSubsection;

/**
 * Стратегия разбора подтаблицы раздела 2.1.
 * @param <T> тип DTO, который возвращает стратегия
 */
public interface TradeLayout<T> {

    /** Соответствует ли стратегия подразделу. */
    boolean isSupports(TradeSubsection subsection);

    /** Распознаёт шапку. true — индексы обновлены. */
    boolean tryUpdate(Row row, DataFormatter fmt);

    /** Валидна ли текущая шапка. */
    boolean isValid();

    /** Сбрасывает состояние при переходе в другой подраздел. */
    void invalidate();

    /** Собирает DTO, если строка похожа на сделку; иначе null. */
    T toTrade(Row row, DataFormatter fmt, SecurityRef security);
}
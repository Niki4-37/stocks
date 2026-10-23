package ru.redcarpet.stocks.reports.dto;

/**
 * Сделка по иностранной валюте (подраздел «Иностранная валюта» раздела 2.1).
 * В шапке две пары «Объём в валюте лота» / «Объём в сопряж. валюте»:
 * первые относятся к покупке, вторые — к продаже.
 */
public record CurrencyTrade(
        String instrument,       // EURRUB_TOM / USDRUB_TOM (строка-идентификатор)
        String lotCurrency,      // «Валюта лота»: EUR / USD
        String counterCurrency,  // «Сопряж. валюта»: Рубль
        String date,             // Дата
        String number,           // Номер
        String buyRate,          // Курс сделки (покупка)
        String buyLotVolume,     // Объём в валюте лота (покупка)
        String buyCounterVolume, // Объём в сопряж. валюте (покупка)
        String sellRate,         // Курс сделки (продажа)
        String sellLotVolume,    // Объём в валюте лота (продажа)
        String sellCounterVolume // Объём в сопряж. валюте (продажа)
) {}

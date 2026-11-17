package ru.redcarpet.stocks.external.cbr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.redcarpet.stocks.external.exception.ExternalServiceException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class CbrXmlParser {

    private static final DateTimeFormatter DYNAMIC_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /** Курсы всех валют на дату из XML_daily.asp (RUB за единицу). */
    public Map<Currency, BigDecimal> parse(String xml) {
        if (xml == null || xml.isBlank()) return Map.of();

        NodeList valutes = parseDocument(xml).getElementsByTagName("Valute");
        Map<Currency, BigDecimal> result = new HashMap<>(valutes.getLength());

        for (int i = 0; i < valutes.getLength(); i++) {
            Element v = (Element) valutes.item(i);

            String charCode = text(v, "CharCode");
            String valueStr = text(v, "Value");
            String nominalStr = text(v, "Nominal");

            if (charCode == null || charCode.isBlank()
                    || valueStr == null || valueStr.isBlank()
                    || nominalStr == null || nominalStr.isBlank()) {
                continue;
            }

            try {
                Currency currency = Currency.getInstance(charCode.trim().toUpperCase());
                BigDecimal rate = unitRate(valueStr, nominalStr);
                if (rate == null) continue;
                result.put(currency, rate);
            } catch (IllegalArgumentException ex) {
                log.warn("Skip CBR valute {}: {}", charCode, ex.getMessage());
            }
        }

        // RUB сам к себе — 1 (ЦБ не публикует RUB, но для кросс-курсов удобно)
        result.putIfAbsent(Currency.getInstance("RUB"), BigDecimal.ONE);
        return result;
    }

    /**
     * Курсы одной валюты за период из XML_dynamic.asp (RUB за единицу).
     * ЦБ отдаёт только рабочие дни, поэтому ключей может быть меньше, чем дней в диапазоне.
     */
    public Map<LocalDate, BigDecimal> parseDynamic(String xml) {
        if (xml == null || xml.isBlank()) return Map.of();

        NodeList records = parseDocument(xml).getElementsByTagName("Record");
        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>(records.getLength());

        for (int i = 0; i < records.getLength(); i++) {
            Element record = (Element) records.item(i);

            String dateStr = record.getAttribute("Date");
            String valueStr = text(record, "Value");
            String nominalStr = text(record, "Nominal");

            if (dateStr == null || dateStr.isBlank()
                    || valueStr == null || valueStr.isBlank()
                    || nominalStr == null || nominalStr.isBlank()) {
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(dateStr.trim(), DYNAMIC_DATE);
                BigDecimal rate = unitRate(valueStr, nominalStr);
                if (rate == null) continue;
                result.put(date, rate);
            } catch (IllegalArgumentException ex) {
                log.warn("Skip CBR record {}: {}", dateStr, ex.getMessage());
            }
        }

        return result;
    }

    /** Соответствие ISO-код валюты → внутренний ID ЦБ из XML_daily.asp, например USD → R01235. */
    public Map<Currency, String> parseCurrencyIds(String xml) {
        if (xml == null || xml.isBlank()) return Map.of();

        NodeList valutes = parseDocument(xml).getElementsByTagName("Valute");
        Map<Currency, String> result = new HashMap<>(valutes.getLength());

        for (int i = 0; i < valutes.getLength(); i++) {
            Element v = (Element) valutes.item(i);

            String id = v.getAttribute("ID");
            String charCode = text(v, "CharCode");

            if (id == null || id.isBlank() || charCode == null || charCode.isBlank()) continue;

            try {
                result.put(Currency.getInstance(charCode.trim().toUpperCase()), id.trim());
            } catch (IllegalArgumentException ex) {
                log.warn("Skip CBR valute id {}: {}", charCode, ex.getMessage());
            }
        }

        return result;
    }

    private static BigDecimal unitRate(String valueStr, String nominalStr) {
        BigDecimal value = new BigDecimal(valueStr.trim().replace(",", "."));
        int nominal = Integer.parseInt(nominalStr.trim());
        if (nominal <= 0) return null;
        return value.divide(BigDecimal.valueOf(nominal), 8, RoundingMode.HALF_UP);
    }

    private static Document parseDocument(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new ExternalServiceException("CBR", "Failed to parse CBR XML", e);
        }
    }

    private static String text(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return null;
        return nl.item(0).getTextContent();
    }
}

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
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CbrXmlParser {

    public Map<Currency, BigDecimal> parse(String xml) {
        if (xml == null || xml.isBlank()) return Map.of();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList valutes = doc.getElementsByTagName("Valute");
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
                    BigDecimal value = new BigDecimal(valueStr.replace(",", "."));
                    int nominal = Integer.parseInt(nominalStr.trim());

                    if (nominal <= 0) continue;
                    BigDecimal rate = value.divide(
                            BigDecimal.valueOf(nominal), 8, RoundingMode.HALF_UP);
                    result.put(currency, rate);
                } catch (IllegalArgumentException ex) {
                    log.warn("Skip CBR valute {}: {}", charCode, ex.getMessage());
                }
            }

            // RUB сам к себе — 1 (ЦБ не публикует RUB, но для кросс-курсов удобно)
            result.putIfAbsent(Currency.getInstance("RUB"), BigDecimal.ONE);
            return result;

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
package ru.redcarpet.stocks.external.cbr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.redcarpet.stocks.external.exception.ExternalServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CbrParsedClient {

    private final RestClient cbrClient;   // из CbrClientConfig
    private final CbrXmlParser parser;

    private static final DateTimeFormatter CBR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Справочник ISO-код → ID ЦБ меняется очень редко, держим его в памяти. */
    private volatile Map<Currency, String> valuteIds;

    /** Возвращает курсы всех валют ЦБ на дату (RUB за единицу). */
    public Map<Currency, BigDecimal> getRatesOnDate(LocalDate date) {
        String xml = fetchXml("/scripts/XML_daily.asp", Map.of(
                "date_req", date.format(CBR_DATE)));
        return parser.parse(xml);
    }

    /**
     * Курсы одной валюты за период (RUB за единицу) одним запросом.
     * Возвращает только дни, за которые ЦБ публиковал курс (рабочие дни).
     */
    public Map<LocalDate, BigDecimal> getRatesForRange(Currency currency,
                                                       LocalDate from,
                                                       LocalDate to) {
        if (from.isAfter(to)) return Map.of();

        Optional<String> valuteId = valuteId(currency);
        if (valuteId.isEmpty()) {
            log.warn("CBR has no valute id for {}", currency);
            return Map.of();
        }

        String xml = fetchXml("/scripts/XML_dynamic.asp", Map.of(
                "date_req1", from.format(CBR_DATE),
                "date_req2", to.format(CBR_DATE),
                "VAL_NM_RQ", valuteId.get()));
        return parser.parseDynamic(xml);
    }

    /** ID валюты во внутреннем справочнике ЦБ (R01235 для USD). */
    public Optional<String> valuteId(Currency currency) {
        return Optional.ofNullable(valuteIds().get(currency));
    }

    private Map<Currency, String> valuteIds() {
        Map<Currency, String> cached = valuteIds;
        if (cached != null) return cached;

        synchronized (this) {
            if (valuteIds == null) {
                // XML_val.asp не содержит ISO-кодов, зато XML_daily.asp отдаёт ID и CharCode рядом
                String xml = fetchXml("/scripts/XML_daily.asp", Map.of(
                        "date_req", LocalDate.now().format(CBR_DATE)));
                valuteIds = Map.copyOf(parser.parseCurrencyIds(xml));
            }
            return valuteIds;
        }
    }

    /** Возвращает XML-ответ ЦБ (plain, раскодированный в windows-1251). */
    private String fetchXml(String path, Map<String, String> query) {
        ResponseEntity<byte[]> resp = cbrClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    query.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .toEntity(byte[].class);

        byte[] body = resp.getBody();
        if (body == null || body.length == 0) {
            throw new ExternalServiceException("CBR", "Empty response for " + path + query);
        }

        String encoding = resp.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if ("gzip".equalsIgnoreCase(encoding)
                && body.length > 2
                && (body[0] & 0xFF) == 0x1F
                && (body[1] & 0xFF) == 0x8B) {
            try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(body))) {
                body = gz.readAllBytes();
            } catch (IOException e) {
                throw new ExternalServiceException("CBR", "Failed to gunzip response", e);
            }
        }

        return new String(body, charsetOf(resp.getHeaders().getContentType()));
    }

    private static Charset charsetOf(MediaType contentType) {
        Charset fallback = Charset.forName("windows-1251");
        if (contentType == null) return fallback;
        try {
            Charset cs = contentType.getCharset();
            return cs != null ? cs : fallback;
        } catch (UnsupportedCharsetException e) {
            return fallback;
        }
    }
}

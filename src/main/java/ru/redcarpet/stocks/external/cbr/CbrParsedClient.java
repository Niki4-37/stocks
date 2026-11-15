package ru.redcarpet.stocks.external.cbr;

import lombok.RequiredArgsConstructor;
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
import java.util.zip.GZIPInputStream;

@Component
@RequiredArgsConstructor
public class CbrParsedClient {

    private final RestClient cbrClient;   // из CbrClientConfig
    private final CbrXmlParser parser;

    private static final DateTimeFormatter CBR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Возвращает курсы всех валют ЦБ на дату (RUB за единицу). */
    public Map<Currency, BigDecimal> getRatesOnDate(LocalDate date) {
        String xml = fetchXml(date);
        return parser.parse(xml);
    }

    /** Возвращает XML-ответ ЦБ (plain, раскодированный в windows-1251). */
    private String fetchXml(LocalDate date) {
        ResponseEntity<byte[]> resp = cbrClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/scripts/XML_daily.asp")
                        .queryParam("date_req", date.format(CBR_DATE))
                        .build())
                .retrieve()
                .toEntity(byte[].class);

        byte[] body = resp.getBody();
        if (body == null || body.length == 0) {
            throw new ExternalServiceException("CBR", "Empty response for " + date);
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
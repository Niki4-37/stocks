package ru.redcarpet.stocks.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoexHistoryService {

    private final RestClient moexClient;

    public List<Map<String, Object>> getHistory (
            String secId,
            LocalDate from,
            LocalDate till,
            String board
    ) {
        String path = String.format(
                "/history/engines/stock/markets/shares/boards/%s/securities/%s.json",
                board,
                secId.toUpperCase()
        );

        List<Map<String, Object>> allRows = new ArrayList<>();
        long start = 0;

        while(true) {
            long currentStart = start;
            log.info("service takes rest client, current start: {}", currentStart);
            MoexResponse response = moexClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("from", from.toString())
                            .queryParam("till", till.toString())
                            .queryParam("start", currentStart)
                            .build())
                    .retrieve()
                    .body(MoexResponse.class);

            if (Objects.isNull(response) || Objects.isNull(response.history())) break;

            List<String> columns = response.history().columns();
            List<List<Object>> rows = response.history().data();

            log.info("get {} rows from response", rows.size());

            if (Objects.isNull(rows) || rows.isEmpty()) break;

            for (var row : rows ) {
                Map<String, Object> localMap = new LinkedHashMap<>();
                for (int i = 0; i < columns.size(); i++) {
                    localMap.put(columns.get(i), row.get(i));
                }
                allRows.add(localMap);
            }

            start += rows.size();

            if (start >= ChronoUnit.DAYS.between(from, till)) break;
        }

        return allRows;
    }

}

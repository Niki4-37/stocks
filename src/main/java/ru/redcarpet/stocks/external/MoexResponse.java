package ru.redcarpet.stocks.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MoexResponse(
        @JsonProperty("history") HistoryBlock history
) {}

package ru.redcarpet.stocks.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MoexResponse(
        @JsonProperty("history") HistoryBlock history
) {}

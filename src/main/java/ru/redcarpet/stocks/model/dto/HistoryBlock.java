package ru.redcarpet.stocks.model.dto;

import java.util.List;

public record HistoryBlock(
   List<String> columns,
   List<List<Object>> data
) {}

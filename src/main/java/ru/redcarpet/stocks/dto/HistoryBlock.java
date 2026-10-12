package ru.redcarpet.stocks.dto;

import java.util.List;

public record HistoryBlock(
   List<String> columns,
   List<List<Object>> data
) {}

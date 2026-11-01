package ru.redcarpet.stocks.external;

import java.util.List;

public record HistoryBlock(
   List<String> columns,
   List<List<Object>> data
) {}

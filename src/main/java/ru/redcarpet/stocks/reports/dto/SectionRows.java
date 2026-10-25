package ru.redcarpet.stocks.reports.dto;

import org.apache.poi.ss.usermodel.Row;

import java.util.List;

public record SectionRows(
        List<Row> cashOperations,        // 1.1
        List<Row> feesAndPenalties,      // 1.3
        List<Row> trades                 // 2.1
) {}
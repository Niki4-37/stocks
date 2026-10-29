package ru.redcarpet.stocks.model.service;

import ru.redcarpet.stocks.model.entity.Security;
import ru.redcarpet.stocks.model.enums.SecurityType;
import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;

public interface SecurityResolver {
    Security resolve(SecurityIdentityFromReport ref, SecurityType type, String exchange);
}

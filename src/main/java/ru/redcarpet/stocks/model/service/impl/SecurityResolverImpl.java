package ru.redcarpet.stocks.model.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.redcarpet.stocks.model.entity.Security;
import ru.redcarpet.stocks.model.enums.SecurityType;
import ru.redcarpet.stocks.model.exception.ValidationException;
import ru.redcarpet.stocks.model.repositories.SecurityRepository;
import ru.redcarpet.stocks.model.service.SecurityResolver;
import ru.redcarpet.stocks.reports.dto.SecurityIdentityFromReport;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityResolverImpl implements SecurityResolver {

    private final SecurityRepository repository;

    @Override
    @Transactional
    public Security resolve(SecurityIdentityFromReport ref, SecurityType type, String exchange) {
        if (ref == null || isBlankOrNull(ref.isin()) && isBlankOrNull(ref.ticker())) {
            throw new ValidationException("SecurityIdentityFromReport must have isin or ticker");
        }

        return findByIsin(ref.isin())
                .or(() -> findByTicker(ref.ticker(), exchange))
                .orElseGet(() -> create(ref, type, exchange));
    }

    private Optional<Security> findByIsin(String isin) {
        return isBlankOrNull(isin) ? Optional.empty() : repository.findByIsin(isin);
    }

    private Optional<Security> findByTicker(String ticker, String exchange) {
        return isBlankOrNull(ticker) ? Optional.empty()
                : repository.findByTickerAndExchange(ticker, exchange);
    }

    private Security create(SecurityIdentityFromReport ref, SecurityType type, String exchange) {
        Security s = new Security();
        s.setTicker(blankToNull(ref.ticker()));
        s.setIsin(blankToNull(ref.isin()));
        s.setName(isBlankOrNull(ref.name()) ? ref.ticker() : ref.name());
        s.setSecurityType(type);
        s.setExchange(blankToNull(exchange));

        try {
            return repository.save(s);
        } catch (DataIntegrityViolationException e) {
            // гонка: кто-то создал параллельно — читаем ещё раз
            return findByIsin(ref.isin())
                    .or(() -> findByTicker(ref.ticker(), exchange))
                    .orElseThrow(() -> new IllegalStateException(
                            "Security disappeared after unique violation: " + ref, e));
        }
    }

    private static boolean isBlankOrNull(String v) {
        return Objects.isNull(v) || v.isBlank();
    }

    private static String blankToNull(String v) {
        return isBlankOrNull(v) ? null : v;
    }
}

package ru.redcarpet.stocks.importing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Выполняет действие в отдельной транзакции ({@code REQUIRES_NEW}).
 * Нужен импортёрам: ошибка в одной строке отчёта не должна помечать
 * rollback-only общую транзакцию и откатывать весь импорт.
 */
@Component
@RequiredArgsConstructor
public class RequiresNewTransaction {

    private final PlatformTransactionManager transactionManager;

    public <T> T execute(Supplier<T> action) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> action.get());
    }
}

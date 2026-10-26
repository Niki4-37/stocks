ALTER TABLE stocks.cash_operations
    ADD COLUMN direction       VARCHAR(8)   NOT NULL DEFAULT 'EXPENSE',
    ADD COLUMN platform        VARCHAR(64),
    ADD COLUMN source          VARCHAR(32)  NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN dividend_issuer VARCHAR(200),
    ADD COLUMN dividend_isin   VARCHAR(12),
    ADD COLUMN dividend_record_date DATE,
    ADD COLUMN dividend_tax    VARCHAR(64);

-- бэкфилл существующих записей: определить direction по operation_type
UPDATE stocks.cash_operations
   SET direction = CASE
       WHEN operation_type IN ('DEPOSIT', 'DIVIDEND', 'COUPON', 'INTEREST') THEN 'INCOME'
       ELSE 'EXPENSE'
   END
 WHERE direction = 'EXPENSE';   -- дефолт, выставленный ADD COLUMN

-- снимаем дефолты, чтобы новые записи явно задавали direction/source
ALTER TABLE stocks.cash_operations
    ALTER COLUMN direction DROP DEFAULT,
    ALTER COLUMN source    DROP DEFAULT;

-- проверки
ALTER TABLE stocks.cash_operations
    ADD CONSTRAINT chk_cash_ops_direction
        CHECK (direction IN ('INCOME', 'EXPENSE'));

-- индекс для UI/сверки
CREATE INDEX ix_cash_ops_portfolio_date
    ON stocks.cash_operations (portfolio_id, operation_date);
ALTER TABLE stocks.events
    ADD COLUMN trade_number VARCHAR(64),
    ADD COLUMN trade_date   DATE,
    ADD COLUMN trade_isin   VARCHAR(12),
    ADD COLUMN trade_source VARCHAR(32);

-- уникальность внешнего ключа в рамках портфеля и источника
CREATE UNIQUE INDEX ux_events_trade_key
    ON stocks.events (portfolio_id, trade_source, trade_number, trade_date, trade_isin)
    WHERE trade_number IS NOT NULL;

-- индекс для быстрого поиска при сверке
CREATE INDEX ix_events_trade_lookup
    ON stocks.events (portfolio_id, trade_number, trade_date);
CREATE TABLE IF NOT EXISTS stocks.currency_trades (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    portfolio_id    BIGINT          NOT NULL,

    instrument      VARCHAR(32)     NOT NULL,      -- EURRUB_TOM
    lot_currency    VARCHAR(3)      NOT NULL,      -- EUR / USD
    counter_currency VARCHAR(3)     NOT NULL,      -- RUB

    trade_number    VARCHAR(64)     NOT NULL,      -- 183730654
    trade_date      DATE            NOT NULL,

    operation_type  VARCHAR(16)     NOT NULL,      -- BUY / SELL
    rate            NUMERIC(19,8),
    lot_volume      NUMERIC(19,6),
    counter_volume  NUMERIC(19,2),

    platform        VARCHAR(64),
    source          VARCHAR(32)     NOT NULL,      -- BROKER_REPORT / MANUAL

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_currency_trades_portfolio
        FOREIGN KEY (portfolio_id) REFERENCES stocks.portfolios (id) ON DELETE CASCADE,

    CONSTRAINT chk_currency_trades_lot_len
        CHECK (char_length(lot_currency) = 3),
    CONSTRAINT chk_currency_trades_counter_len
        CHECK (char_length(counter_currency) = 3),
    CONSTRAINT chk_currency_trades_type
        CHECK (operation_type IN ('BUY', 'SELL'))
);

CREATE UNIQUE INDEX ux_currency_trades_number
    ON stocks.currency_trades (portfolio_id, source, trade_number);

CREATE INDEX ix_currency_trades_portfolio_date
    ON stocks.currency_trades (portfolio_id, trade_date);
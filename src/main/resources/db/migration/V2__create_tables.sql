CREATE TABLE IF NOT EXISTS stocks.portfolios (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name            VARCHAR(100)    NOT NULL,
    base_currency   VARCHAR(3)      NOT NULL,

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL,

    CONSTRAINT chk_portfolios_name_not_blank
        CHECK (length(btrim(name)) > 0),

    CONSTRAINT chk_portfolios_base_currency_len
        CHECK (char_length(base_currency) = 3)
);

CREATE TABLE IF NOT EXISTS stocks.portfolio_balances (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    portfolio_id    BIGINT          NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    amount          NUMERIC(19,2)   NOT NULL DEFAULT 0,

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL,

    CONSTRAINT fk_balances_portfolio
        FOREIGN KEY (portfolio_id)
        REFERENCES stocks.portfolios (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stocks.securities (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticker          VARCHAR(20),
    isin            VARCHAR(12),
    figi            VARCHAR(12),
    name            VARCHAR(200)    NOT NULL,
    security_type   VARCHAR(32)     NOT NULL,
    exchange        VARCHAR(32),                -- MOEX, NYSE, ...

    CONSTRAINT chk_securities_exchange
        CHECK (exchange IS NULL OR exchange ~ '^[A-Z0-9_-]{2,32}$')
);

CREATE TABLE IF NOT EXISTS stocks.events (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    portfolio_id    BIGINT          NOT NULL,
    security_id     BIGINT,

    operation_type  VARCHAR(32)     NOT NULL,
    event_date      DATE            NOT NULL,

    price           NUMERIC(19,6)   NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    exchange_rate   NUMERIC(19,8),
    quantity        NUMERIC(19,6),

    amount          NUMERIC(19,2)   NOT NULL,

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL,

    CONSTRAINT fk_events_portfolio
        FOREIGN KEY (portfolio_id)
        REFERENCES stocks.portfolios (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_events_securities
        FOREIGN KEY (security_id)
        REFERENCES stocks.securities (id),

    CONSTRAINT chk_events_currency_len
        CHECK (char_length(currency) = 3),

    CONSTRAINT chk_events_price_non_negative
        CHECK (price >= 0),

    CONSTRAINT chk_events_exchange_rate_positive
        CHECK (exchange_rate IS NULL OR exchange_rate > 0),

    CONSTRAINT chk_events_quantity_positive
        CHECK (quantity IS NULL OR quantity > 0)
);

CREATE TABLE IF NOT EXISTS stocks.holdings (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    portfolio_id    BIGINT          NOT NULL,
    security_id     BIGINT          NOT NULL,

    quantity        NUMERIC(19,6)   NOT NULL,
    avg_price       NUMERIC(19,6)   NOT NULL,
    currency        VARCHAR(3)      NOT NULL,

    total_dividends NUMERIC(19,2)   NOT NULL DEFAULT 0,
    total_coupons   NUMERIC(19,2)   NOT NULL DEFAULT 0,
    realized_pnl    NUMERIC(19,2)   NOT NULL DEFAULT 0,

    first_buy_date  DATE,
    last_event_date DATE,

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL,

    CONSTRAINT fk_holdings_portfolio
        FOREIGN KEY (portfolio_id)
        REFERENCES stocks.portfolios (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_holdings_securities
        FOREIGN KEY (security_id)
        REFERENCES stocks.securities (id),

    CONSTRAINT chk_holdings_currency_len
        CHECK (char_length(currency) = 3)
);

CREATE TABLE IF NOT EXISTS stocks.cash_operations (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    portfolio_id    BIGINT          NOT NULL,

    operation_type  VARCHAR(32)     NOT NULL,
    operation_date  DATE            NOT NULL,

    currency        VARCHAR(3)      NOT NULL,
    amount          NUMERIC(19,2)   NOT NULL,

    target_currency VARCHAR(3),       -- только CURRENCY_EXCHANGE
    target_amount   NUMERIC(19,2),    -- только CURRENCY_EXCHANGE
    exchange_rate   NUMERIC(19,8),    -- только CURRENCY_EXCHANGE

    comment         VARCHAR(500),

    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_cash_ops_portfolio
        FOREIGN KEY (portfolio_id)
        REFERENCES stocks.portfolios (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_cash_ops_currency_len
        CHECK (char_length(currency) = 3),
    CONSTRAINT chk_cash_ops_target_currency_len
        CHECK (target_currency IS NULL OR char_length(target_currency) = 3),

    CONSTRAINT chk_cash_ops_amount_positive
        CHECK (amount > 0),
    CONSTRAINT chk_cash_ops_target_amount_positive
        CHECK (target_amount IS NULL OR target_amount > 0),

    CONSTRAINT chk_cash_ops_exchange_fields
        CHECK (
            (operation_type = 'CURRENCY_EXCHANGE'
                AND target_currency IS NOT NULL
                AND target_amount IS NOT NULL
                AND currency <> target_currency)
            OR
            (operation_type <> 'CURRENCY_EXCHANGE'
                AND target_currency IS NULL
                AND target_amount IS NULL
                AND exchange_rate IS NULL)
        ),

    CONSTRAINT chk_cash_ops_exchange_rate_positive
        CHECK (exchange_rate IS NULL OR exchange_rate > 0)
);
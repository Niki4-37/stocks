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
    security_id     BIGINT          NOT NULL,

    operation_type  VARCHAR(32)     NOT NULL,
    event_date      DATE            NOT NULL,

    price           NUMERIC(19,6)   NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    exchange_rate   NUMERIC(19,8),
    quantity        NUMERIC(19,6),

    amount          NUMERIC(19,2)   NOT NULL,
    amount_in_base  NUMERIC(19,2)   NOT NULL,

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

    CONSTRAINT chk_holdings_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT chk_holdings_avg_price_non_negative
        CHECK (avg_price >= 0),
    CONSTRAINT chk_holdings_currency_len
        CHECK (char_length(currency) = 3)
);
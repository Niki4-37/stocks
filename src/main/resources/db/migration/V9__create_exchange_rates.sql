CREATE TABLE IF NOT EXISTS stocks.exchange_rates (
   id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
   rate_date       DATE            NOT NULL,
   currency        VARCHAR(3)      NOT NULL,       -- USD, EUR, ...
   base_currency   VARCHAR(3)      NOT NULL DEFAULT 'RUB',
   rate            NUMERIC(19,8)   NOT NULL,       -- за 1 единицу currency в base_currency
   source          VARCHAR(32)     NOT NULL,       -- CBR, BROKER_REPORT, MANUAL
   created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

   CONSTRAINT chk_exchange_rates_currency_len
       CHECK (char_length(currency) = 3),
   CONSTRAINT chk_exchange_rates_rate_positive
       CHECK (rate > 0)
);

CREATE UNIQUE INDEX ux_exchange_rates_key
   ON stocks.exchange_rates (rate_date, currency, base_currency, source);
CREATE UNIQUE INDEX ux_securities_isin
    ON stocks.securities (isin)
    WHERE isin IS NOT NULL;

CREATE UNIQUE INDEX ux_securities_ticker_exchange
    ON stocks.securities (ticker, exchange)
    WHERE ticker IS NOT NULL;
CREATE OR REPLACE FUNCTION stocks.normalize_security_name(text)
RETURNS text
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT lower(
        trim(
            regexp_replace(
                regexp_replace(
                    regexp_replace(trim($1), '\m(inc|corp|corporation|llc|ltd|plc|co)\M\.?', '', 'gi'),
                    '[.,]', '', 'g'
                ),
                '\s+', ' ', 'g'
            )
        )
    );
$$;

CREATE INDEX ix_securities_name_normalized
    ON stocks.securities (stocks.normalize_security_name(name));
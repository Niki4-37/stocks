DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'stocks'
      AND table_name = 'cash_operations'
      AND column_name = 'target_currency'
  ) THEN
    ALTER TABLE stocks.cash_operations DROP COLUMN target_currency;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'stocks'
      AND table_name = 'cash_operations'
      AND column_name = 'target_amount'
  ) THEN
    ALTER TABLE stocks.cash_operations DROP COLUMN target_amount;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'stocks'
      AND table_name = 'cash_operations'
      AND column_name = 'exchange_rate'
  ) THEN
    ALTER TABLE stocks.cash_operations DROP COLUMN exchange_rate;
  END IF;
END $$;

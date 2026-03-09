-- ============================================================
--  V1__create_payments_table.sql
--  Migración inicial: tabla de pagos
-- ============================================================

CREATE TABLE IF NOT EXISTS payments (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    debt_id        INTEGER NOT NULL,
    amount         REAL    NOT NULL,
    payment_date   TEXT    NOT NULL,   -- ISO-8601 UTC
    note           TEXT,
    created_at     TEXT    NOT NULL    -- ISO-8601 UTC
);

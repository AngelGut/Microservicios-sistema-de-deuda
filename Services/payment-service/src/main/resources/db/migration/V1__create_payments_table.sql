-- ============================================================
--  V1__create_payments_table.sql
--  Migración inicial: tabla de pagos
-- ============================================================

CREATE TABLE IF NOT EXISTS payments (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    debt_id        TEXT    NOT NULL,   -- UUID string from debt-service
    amount         REAL    NOT NULL,
    payment_date   TEXT    NOT NULL,   -- ISO-8601 UTC
    note           TEXT,
    created_at     TEXT    NOT NULL    -- ISO-8601 UTC
);

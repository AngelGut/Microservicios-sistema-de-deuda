-- ============================================================
--  V1__create_client_risk_table.sql
--  Tabla principal de riesgo crediticio por cliente
-- ============================================================

CREATE TABLE IF NOT EXISTS client_risk (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    client_id           INTEGER NOT NULL UNIQUE,
    risk_level          TEXT    NOT NULL,
    risk_score          REAL    NOT NULL DEFAULT 0,
    total_days_late     INTEGER NOT NULL DEFAULT 0,
    late_payment_count  INTEGER NOT NULL DEFAULT 0,
    payment_count       INTEGER NOT NULL DEFAULT 0,
    last_calculated_at  TEXT    NOT NULL,
    created_at          TEXT    NOT NULL,
    updated_at          TEXT    NOT NULL
);

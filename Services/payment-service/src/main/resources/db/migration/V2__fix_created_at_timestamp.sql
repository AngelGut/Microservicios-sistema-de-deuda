-- ============================================================
--  V2__fix_created_at_timestamp.sql
--  Migración para arreglar el problema de timestamp en SQLite
--  Elimina tabla vieja con Instant incorrecto y la recrea
-- ============================================================

-- Eliminar tabla vieja (sqlite no soporta ALTER COLUMN bien)
DROP TABLE IF EXISTS payments;

-- Recrear tabla con el esquema correcto
CREATE TABLE payments (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    debt_id        TEXT    NOT NULL,
    amount         REAL    NOT NULL,
    payment_date   TEXT    NOT NULL,
    note           TEXT,
    created_at     TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para mejorar queries
CREATE INDEX idx_payments_debt_id ON payments(debt_id);
CREATE INDEX idx_payments_created_at ON payments(created_at);

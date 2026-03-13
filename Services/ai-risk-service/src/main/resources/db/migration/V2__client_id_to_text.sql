-- V2: migrar client_id de INTEGER a TEXT para soportar UUIDs
CREATE TABLE client_risk_new (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    client_id           TEXT    NOT NULL UNIQUE,
    risk_level          TEXT    NOT NULL,
    risk_score          REAL    NOT NULL DEFAULT 0,
    total_days_late     INTEGER NOT NULL DEFAULT 0,
    late_payment_count  INTEGER NOT NULL DEFAULT 0,
    payment_count       INTEGER NOT NULL DEFAULT 0,
    last_calculated_at  TEXT    NOT NULL,
    created_at          TEXT    NOT NULL,
    updated_at          TEXT    NOT NULL
);

INSERT INTO client_risk_new
SELECT id, CAST(client_id AS TEXT), risk_level, risk_score,
       total_days_late, late_payment_count, payment_count,
       last_calculated_at, created_at, updated_at
FROM client_risk;

DROP TABLE client_risk;
ALTER TABLE client_risk_new RENAME TO client_risk;

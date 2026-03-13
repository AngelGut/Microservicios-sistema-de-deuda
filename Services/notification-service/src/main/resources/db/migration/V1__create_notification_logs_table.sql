-- ============================================================
--  notification-service
--  V1__create_notification_logs_table.sql
-- ============================================================
--  Historial de notificaciones enviadas por el sistema.
--  Permite auditoría y evita envíos duplicados.
-- ============================================================

CREATE TABLE IF NOT EXISTS notification_logs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    type            TEXT    NOT NULL,           -- PAYMENT_CONFIRMATION | PAYMENT_REMINDER
    reference_id    TEXT,                       -- ID de la deuda o pago relacionado
    recipient_email TEXT    NOT NULL,
    recipient_name  TEXT,
    status          TEXT    NOT NULL,           -- SENT | FAILED
    message         TEXT,
    processed_at    TEXT    NOT NULL,           -- ISO-8601 datetime
    created_at      TEXT    NOT NULL            -- ISO-8601 datetime
);

CREATE INDEX IF NOT EXISTS idx_notif_type_ref_status
    ON notification_logs (type, reference_id, status, processed_at);

CREATE INDEX IF NOT EXISTS idx_notif_recipient
    ON notification_logs (recipient_email);

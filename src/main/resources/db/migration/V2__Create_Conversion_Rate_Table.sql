CREATE TABLE conversion_rates
(
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency_id   INTEGER        NOT NULL,
    target_currency_id INTEGER        NOT NULL,
    rate               DECIMAL(16, 6) NOT NULL,
    CONSTRAINT fk_conversion_rates_base_currency FOREIGN KEY (base_currency_id) REFERENCES currencies (id),
    CONSTRAINT fk_conversion_rates_target_currency FOREIGN KEY (target_currency_id) REFERENCES currencies (id),
    CONSTRAINT uq_conversion_rates_base_target UNIQUE (base_currency_id, target_currency_id),
    CHECK (rate >= 0)
)
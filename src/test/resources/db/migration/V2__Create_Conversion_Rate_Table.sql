CREATE TABLE conversion_rates
(
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency   INTEGER NOT NULL,
    target_currency INTEGER NOT NULL,
    rate            DECIMAL(16, 6) NOT NULL,
    CONSTRAINT fk_conversion_rates_base_currency FOREIGN KEY (base_currency) REFERENCES currencies (id),
    CONSTRAINT fk_conversion_rates_target_currency FOREIGN KEY (target_currency) REFERENCES currencies (id),
    CONSTRAINt uq_conversion_rates_base_target UNIQUE (base_currency, target_currency),
    check (rate >= 0)
)
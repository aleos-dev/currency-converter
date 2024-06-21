INSERT INTO currencies (code, fullname, sign) VALUES ('USD', 'United States Dollar', '$');
INSERT INTO currencies (code, fullname, sign) VALUES ('EUR', 'Euro', '€');
INSERT INTO currencies (code, fullname, sign) VALUES ('JPY', 'Japanese Yen', '¥');
INSERT INTO currencies (code, fullname, sign) VALUES ('GBP', 'British Pound', '£');
INSERT INTO currencies (code, fullname, sign) VALUES ('AUD', 'Australian Dollar', 'A$');
INSERT INTO currencies (code, fullname, sign) VALUES ('CAD', 'Canadian Dollar', 'C$');
INSERT INTO currencies (code, fullname, sign) VALUES ('CHF', 'Swiss Franc', 'CHF');
INSERT INTO currencies (code, fullname, sign) VALUES ('CNY', 'Chinese Yuan', '¥');
INSERT INTO currencies (code, fullname, sign) VALUES ('RUB', 'Russian Ruble', '₽');
INSERT INTO currencies (code, fullname, sign) VALUES ('UAH', 'Ukrainian Hryvnia', '₴');

INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 2, 0.85); -- USD to EUR
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 3, 110.15); -- USD to JPY
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 4, 0.75); -- USD to GBP
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 5, 1.35); -- USD to AUD
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 6, 1.25); -- USD to CAD
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 7, 0.92); -- USD to CHF
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 8, 6.45); -- USD to CNY
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 9, 74.25); -- USD to RUB
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 10, 27.90); -- USD to UAH
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (2, 3, 129.53); -- EUR to JPY

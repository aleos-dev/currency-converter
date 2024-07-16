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

INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 2, 0.93); -- USD to EUR
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 3, 161.61); -- USD to JPY
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 4, 0.79); -- USD to GBP
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 5, 1.35); -- USD to AUD
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 6, 1.25); -- USD to CAD
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 7, 0.92); -- USD to CHF
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 8, 7.27); -- USD to CNY
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 9, 86.75); -- USD to RUB
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (1, 10, 40.9); -- USD to UAH
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (2, 3, 173.32); -- EUR to JPY
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (3, 8 , 0.045); -- JPY to CNY
INSERT INTO conversion_rates (base_currency_id, target_currency_id, rate) VALUES (10, 8 , 0.18); -- UAH to CNY

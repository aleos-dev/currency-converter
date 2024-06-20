CREATE TABLE currencies
(
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    fullname TEXT NOT NULL UNIQUE,
    code TEXT NOT NULL UNIQUE,
    sign TEXT NOT NULL,
    CHECK (length(code) == 3 AND length(fullname) <= 30 AND length(sign) <= 30)
)
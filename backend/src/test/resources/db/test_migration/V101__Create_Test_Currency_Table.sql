CREATE TABLE currencies
(
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    fullname TEXT NOT NULL UNIQUE,
    code     TEXT NOT NULL UNIQUE,
    sign     TEXT NOT NULL,
    CHECK (LENGTH(code) == 3 AND LENGTH(fullname) <= 30 AND LENGTH(sign) <= 30)
)
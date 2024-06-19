CREATE TABLE currencies
(
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    code TEXT NOT NULL UNIQUE,
    sign TEXT NOT NULL,
    CHECK (length(Code) == 3 AND length(Name) <= 30 AND length(Sign) <= 30)
)
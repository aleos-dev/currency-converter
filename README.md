
# Exchange Rates API

This API allows you to interact with exchange rates and currencies. Below are the available endpoints and their usage.

## Index

### Exchange Rates
- [ ] [Get All Exchange Rates](#get-all-exchange-rates)
- [ ] [Get Specific Exchange Rate](#get-specific-exchange-rate)
- [ ] [Add New Exchange Rate](#add-new-exchange-rate)
- [ ] [Update Existing Exchange Rate](#update-existing-exchange-rate)
- [ ] [Currency Exchange Calculation](#currency-exchange-calculation)

### Currencies
- [ ] [Get All Currencies](#get-all-currencies)
- [x] [Get Specific Currency](#get-specific-currency)
- [ ] [Add New Currency](#add-new-currency)

## Endpoints

### Exchange Rates

#### Get All Exchange Rates

**Endpoint:** `GET /exchangeRates`

**Description:** Retrieves a list of all exchange rates.

**Response Example:**
```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```

**HTTP Response Codes:**
- `200 OK` - Success
- `500 Internal Server Error` - Error (e.g., database unavailable)

#### Get Specific Exchange Rate

**Endpoint:** `GET /exchangeRate/{baseCurrencyCode}{targetCurrencyCode}`

**Description:** Retrieves the exchange rate for a specific currency pair. The currency pair is specified by the consecutive currency codes in the request path.

**Example Request:** `GET /exchangeRate/USDEUR`

**Response Example:**
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```

**HTTP Response Codes:**
- `200 OK` - Success
- `400 Bad Request` - Currency codes missing in the request
- `404 Not Found` - Exchange rate not found for the pair
- `500 Internal Server Error` - Error (e.g., database unavailable)

#### Add New Exchange Rate

**Endpoint:** `POST /exchangeRates`

**Description:** Adds a new exchange rate to the database. Data is submitted in the request body as form fields (x-www-form-urlencoded).

**Form Fields:**
- `baseCurrencyCode` - e.g., USD
- `targetCurrencyCode` - e.g., EUR
- `rate` - e.g., 0.99

**Response Example:**
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```

**HTTP Response Codes:**
- `201 Created` - Success
- `400 Bad Request` - Missing required form field
- `409 Conflict` - Exchange rate for the pair already exists
- `404 Not Found` - One or both currencies do not exist in the database
- `500 Internal Server Error` - Error (e.g., database unavailable)

#### Update Existing Exchange Rate

**Endpoint:** `PATCH /exchangeRate/{baseCurrencyCode}{targetCurrencyCode}`

**Description:** Updates an existing exchange rate in the database. The currency pair is specified by the consecutive currency codes in the request path. Data is submitted in the request body as form fields (x-www-form-urlencoded).

**Form Fields:**
- `rate` - e.g., 0.99

**Response Example:**
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```

**HTTP Response Codes:**
- `200 OK` - Success
- `400 Bad Request` - Missing required form field
- `404 Not Found` - Currency pair not found in the database
- `500 Internal Server Error` - Error (e.g., database unavailable)

#### Currency Exchange Calculation

**Endpoint:** `GET /exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`

**Description:** Calculates the conversion of a specified amount from one currency to another.

**Example Request:** `GET /exchange?from=USD&to=AUD&amount=10`

**Response Example:**
```json
{
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A€"
    },
    "rate": 1.45,
    "amount": 10.00,
    "convertedAmount": 14.50
}
```

**HTTP Response Codes:**
- `200 OK` - Success
- `400 Bad Request` - Missing required query parameters
- `404 Not Found` - Exchange rate not found for the pair
- `500 Internal Server Error` - Error (e.g., database unavailable)

### Currencies

#### Get All Currencies

**Endpoint:** `GET /currencies`

**Description:** Retrieves a list of all currencies.

**Response Example:**
```json
[
    {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },   
    {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
]
```

**HTTP Response Codes:**
- `200 OK` - Success
- `500 Internal Server Error` - Error (e.g., database unavailable)

#### Get Specific Currency

**Endpoint:** `GET /currency/{currencyCode}`

**Description:** Retrieves details of a specific currency.

**Example Request:** `GET /currency/EUR`

**Response Example:**
```json
{
    "id": 1,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```

**HTTP Response Codes:**
- `200 OK` - Success
- `400 Bad Request` - Currency code missing in the request
- `404 Not Found` - Currency not found
- `500 Internal Server Error` - Error (e.g., database unavailable)

#### Add New Currency

**Endpoint:** `POST /currencies`

**Description:** Adds a new currency to the database. Data is submitted in the request body as form fields (x-www-form-urlencoded).

**Form Fields:**
- `name` - e.g., Euro
- `code` - e.g., EUR
- `sign` - e.g., €

**Response Example:**
```json
{
    "id": 1,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```

**HTTP Response Codes:**
- `201 Created` - Success
- `400 Bad Request` - Missing required form field
- `409 Conflict` - Currency with the same code already exists
- `500 Internal Server Error` - Error (e.g., database unavailable)

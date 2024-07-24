# Currency Convertor

This project is an emulation of a currency converter that utilizes an in-memory database to persist data during a single runtime session. It provides various APIs for handling currencies, exchange rates, and conversions.

## Index
[Project Objective](#project-objective)

[Getting Started](#getting-started)

[Reflection on the project](architectural-approach)
  - [approach](#initialization-with-applicationstartuplistener)
  - [dao layer](#reflections-on-dao-layer)
  - [service layer](#reflections-on-service-layer)
  - [servlets](#servlets)
  - [filters](#filters)

[Exchange API](#exchange-api)

[Currency API](#currency-api)

[Exchange Rate API](#exchange-rate-api)

## Project Objective
The aim of this project is to learn how to process HTTP requests with a focus on using servlet APIs. It is essential to understand the workflow involved in processing client requests.The project utilizes various tools:

- **Servlet API**: Provides a standard way to handle HTTP requests and responses. 
- **Tomcat**: A robust servlet container that implements the Servlet API. Tomcat serves as a web server and provides an 
  environment where Java code can run.
- **FlywayDB**: Manages database migrations. It ensures that the database state is consistent and aligned with the 
  current version of the application.
- **SQLite**: In this project, SQLite serves as an in-memory database to store data persistently during runtime.
- **HikariCP**: A JDBC connection pool that manages a pool of database connections, improving the performance of 
  database operations by reusing connections rather than creating new ones for every request.
- **ModelMapper**: Simplifies the task of mapping objects to each other.
- **GSON**: A Java library used to convert Java Objects into their JSON representation and vice versa.
- **Maven**: A build automation tool used primarily for Java projects.

## Getting Started

## Architectural Approach

#### Initialization with ApplicationStartupListener:
`The ApplicationStartupListener is utilized to initialize the context with essential components such as the data 
source and object mapper.`

`This object contains a predefined list of components necessary for the application's 
functionality and relies on ComponentInitializerUtil to instantiate these components.`

`It also handles the manual registration of servlets, aiming to dynamically set servlet mappings, which cannot be achieved through annotations or web.xml.`

#### Component Instantiation via ComponentInitializerUtil:
`ComponentInitializerUtil leverages reflection to instantiate objects and utilizes the ServletContext for dependency lookup and RequestAttributeUtil to resolve component naming.`

#### Database Configuration with DatabaseUtil:
`DatabaseUtil establishes an in-memory SQLite database instance, enhanced by HikariCP for efficient connection pooling. It also manages database schema evolution using Flyway.`

#### Property Management with PropertiesUtil:
`PropertiesUtil acts as the property source for the application, loading configurations from the application.properties file.`

### Reflections on the DAO Layer

Despite not requiring all CRUD operations per project specifications, I  decided to implement them to gain experience with the interplay between entities facilitated by an abstract CRUD class. This approach allowed me to extract common logic into a base class, enhancing code reuse and maintainability.

 #### Exposure of Entity Identifiers:
`The implementation exposed both the surrogate and natural keys of entities. However, this led to challenges in managing both of them, termed "identifier dualism." Therefore, this issue should be taken into consideration in future implementations.`

#### Abstract Class for CRUD Operations:
`To centralize common CRUD functionalities, an abstract class was employed, necessitating that all entities implement a generic interface which defines getId() and setId() methods. This design choice allowed CRUD operations related to the entity's ID to be abstracted into the parent class.`
#### Database Operations: Complexity vs. Efficiency
`Single statement: To minimize complexity at the application level, I substantially increased the complexity of SQL 
  statements. This 
  approach, demonstrated in the SELECT_CROSS_RATE_BY_CODES method, involves sophisticated SQL constructs such as subqueries and joins to encapsulate complex business logic within the database. While this reduces the need for additional application logic to process data, it makes the SQL statements themselves more complex and potentially harder to maintain.`
 
`Transaction Management: Implementing multiple operations within a single transaction in the saveAndFetch method 
emphasized the need for robust error handling and effective transaction management. This approach ensures data integrity and consistency but requires careful management of transaction boundaries and error scenarios. It involves a meticulous design to handle rollbacks and exceptions efficiently, thus safeguarding against data anomalies and operational failures.`

`Performance Considerations: It's not always clear which approach—simpler SQL with more application logic versus 
  complex SQL with minimal application processing—will yield better performance.This ambiguity results in a trade-off.`

#### Intermittent SQLite Errors:
`During development, I encountered sporadic errors related to disappearing tables during save and update operations. This issue proved to be elusive and remains unresolved.`
###### Solved: It appears that HikariCP closes idle connections, causing the SQLite in-memory database to be lost. Consequently, when HikariCP tries to connect on demand, it creates a new in-memory database without the migrations. This was resolved by setting the minimumIdle property of HikariCP to 1.

#### Boolean Returns in CRUD Operations:
`CRUD methods were designed to return a boolean to indicate success or failure. Such a binary indicator often falls 
  short in situations where detailed, nuanced feedback is required.`

### Reflections on the Service Layer

The service layer in this application primarily mirrors the functionality of the DAO layer due to the simplicity of 
the application requirements. This layer handles both DTOs and entities, utilizing a ModelMapper instance to convert between these types.

#### Integration Challenges: 
`The service layer revealed some complexities concerning the interaction between Data 
Transfer Objects (DTOs) and Data Access Objects (DAOs), where the required interfaces are not always compatible.`

`For instance, while a conversion rate DTO might only include natural keys for identifying currencies, the 
corresponding entity requires complete currency instances.`

`This discrepancy means that methods like save(ConversionRate) or update(ConversionRate) cannot be directly utilized without first constructing full currency objects.`

`To resolve this, there is either a need to create new DAO methods that cater to these requirements or to implement workarounds, both of which may be seen as less than ideal. This scenario poses a significant design decision: should the API be adapted to include more user-friendly methods, or should the responsibility be shifted to the clients to manage the complexities of providing what the DAO needs?`

#### Complexity in ModelMapper Conversion: 
`The use of ModelMapper exposed limitations when dealing with Java records due to its reliance on reflection, which is not optimized for the immutable properties of records. This led to a manual configuration of DTO-to-entity mappings.`

#### Caching Mechanism:
`The implementation of CacheService was intended to simulate the behavior of a caching layer. It 
provides insights into caching operations through console logs when enabled. This approach serves educational purposes.`

### Servlets
I try to keep this layer as simple as it can be. The validation, payload extraction and response composing take out 
to filter responsibility. It generally calls service and handles result in proper way.

#### BaseServlet: 
`All main servlets extend an abstract BaseServlet, which overrides the default init() method to inject dependencies.` 

#### CustomDefaultServlet: 
`It is a customized version of the DefaultServlet. The DefaultServlet is designed to serve 
static files and acts as a fallback when the servlet container cannot find a matching mapping for the current 
request. It typically handles GET and HEAD methods intended for fetching resources.`

`However, unexpected error codes were encountered for non-existent resources: 405(PUT, POST, etc.), 404(GET, HEAD), and 501(PATCH) methods.`

`To align this behavior with typical expectations—where non-existent resources should result in a 404 Not Found 
response—the DefaultServlet behavior was overridden in the CustomDefaultServlet. This may be debatable, but it was 
made for educational purposes. So, no harm.`

#### Error Handling: 
Apart from the base servlets, the servlet container's capabilities are utilized to handle error 
codes. There are two main variants for error handling:

`Error 404: This is handled by the Error404Servlet, which provides a custom JSON response with a humorous message.`

`Error 405: This is managed using a static resource, error405.jsp, to inform users that the method is not allowed.`

Error pages are declared in the web.xml configuration file.

### Filters
Understanding the role of filters in web application development is crucial. Filters do much more than modify incoming requests—they are essential in managing various aspects of request processing.

#### Common filters
These filters handle fundamental operations that are critical to the application's infrastructure.

**EncodingFilter**: 
`This filter sets the character encoding used in the body of both the request and the response.`

**ExceptionHandlingFilter**:
`This filter catches exceptions that occur during the runtime of the program and cannot be resolved on-site. It composes a proper response to the client, ensuring that unhandled exceptions do not crash the application.`

**JsonResponseFilter**:
`Applied after servlet processing, this filter is responsible for writing the response in JSON format if the RequestContext contains a response object under a specified attribute.`

**CorsFilter**:
`Cross-Origin Resource Sharing (CORS) is an HTTP-header based mechanism that allows a server to indicate which origins (domain, scheme, or port) other than its own are permitted to access its resources. This filter sets the appropriate headers to enable the backend to accept requests from clients on different domains.`

**CachingFilter**:
`This filter checks if a cached response is available for the request. If so, it returns the cached response; if not, it proceeds with the next filter. After the servlet processes the request, the response is saved in the cache. The filter also invalidates the current cache state for methods that change data, such as POST or PATCH. The CacheService is used for caching, though it could be simplified by using ServletContext to store cache entries.`

#### Servlet-Specific Filters:
These filters are bound to specific servlets and prepare the working environment for them. Their primary function is to extract and validate payload objects through injected validators.

    CurrenciesUrlFilter
    CurrencyUrlFilter
    ConversionRateUrlFilter
    ConversionUrlFilter

Each filter ensures the incoming request data is correctly formatted and validated before it is processed by the 
servlets. They are registered using web.xml, as @WebFilter cannot guarantee the order.

### Deployment
`Docker simplifies running parts of my application, and I appreciate its convenience. This enthusiasm led me to package my application into Docker Hub. Afterwards, I pulled the images onto my server and ran Docker Compose. Initially, everything seemed fine, but over time, the server began to experience issues and eventually hung, necessitating a reboot. After investigating, I discovered that the 1 GB of RAM on my server was insufficient for Docker, as it operates heavily in memory.`

`Consequently, I reverted to using my previously operational Apache server, which was running WordPress. Unsure how to serve a WAR file on Apache, I decided to run Tomcat in the background. I then configured Apache to handle only the frontend, directing it to localhost. I also set up Apache to proxy requests to the backend via SSL certificates, sparing me from configuring SSL on Tomcat. Lastly, I exposed the backend on port 9091, but I am aware it’s not secure to access it directly outside of the frontend setup.`

Note:
`Upon revisiting the issues with Docker's abundant resource consumption, I discovered that MySQL, which supports the WordPress site (containing about 10KB of content), was consuming approximately 400MB of RAM. This excessive usage was due to a feature known as the "Performance Schema," which provides detailed runtime diagnostics. Considering that my WordPress is a minimal setup for personal use, I disabled this feature. I achieved this by setting performance_schema = 0 in the /etc/mysql/mysql.conf.d/mysqld.conf file. After this adjustment, the memory consumption dropped by more than half.`

# Exchange Rates API

This API allows you to interact with exchange rates and currencies. Below are the available endpoints and their usage.

#### Exchange Rate API
- [x] [GET All Exchange Rates](#get-all-exchange-rates)
- [x] [GET Specific Exchange Rate](#get-specific-exchange-rates)
- [x] [Add New Exchange Rate](#add-new-exchange-rate)
- [x] [Update Existing Exchange Rate](#update-existing-exchange-rate)
- [x] [Delete Existing Exchange Rate](#delete-existing-exchange-rate)

#### Exchange API
- [x] [Currency Exchange Calculation](#currency-exchange-calculation)

#### Currency API
- [x] [GET All Currencies](#get-all-currencies)
- [x] [GET Specific Currency](#get-specific-currency)
- [x] [Add New Currency](#add-new-currency)
- [x] [Update Existing Currency](#update-existing-currency)
- [x] [Delete Existing Currency](#delete-existing-currency)

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

**Description:** Retrieves the exchange rate for a specific currency pair. The currency pair is specified by the
consecutive currency codes in the request path.

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

**Description:** Adds a new exchange rate to the database. Data is submitted in the request body as form fields (
x-www-form-urlencoded).

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

**Description:** Updates an existing exchange rate in the database. The currency pair is specified by the consecutive
currency codes in the request path. Data is submitted in the request body as form fields (x-www-form-urlencoded).

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


#### Delete Existing Exchange Rate

**Endpoint:** `DELETE /exchangeRate/{id}`

**Description:** Delete an existing exchange rate in the database. The currency is specified by the integer identifier in the request path.

**Example Request:** `DELETE /exchangeRate/1`

**HTTP Response Codes:**

- `204 OK` - Success
- `400 Bad Request` - Missing or invalid a required path identifier
- `404 Not Found` - ExchangeRate not found in the database
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

**Description:** Adds a new currency to the database. Data is submitted in the request body as form fields (
x-www-form-urlencoded).

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
- 
#### Update Existing Currency

**Endpoint:** `PATCH /currency/{id}

**Description:** Updates an existing currency in the database. The currency is specified by the integer identifier 
in the request path. Data is submitted in the request body as form fields (x-www-form-urlencoded).

**Form Fields:**

- `name` - e.g., Dollar
- `code` - e.g., USD
- `sign` - e.g., $


**HTTP Response Codes:**

- `204 OK` - Success
- `400 Bad Request` - Missing required form field or invalid identifier
- `404 Not Found` - Currency not found in the database
- `500 Internal Server Error` - Error (e.g., database unavailable)


#### Delete Existing Currency

**Endpoint:** `DELETE /currency/{id}`

**Description:** Delete an existing currency in the database. The currency is specified by the integer identifier in the request path.

**Example Request:** `DELETE /exchangeRate/1`

**HTTP Response Codes:**

- `204 OK` - Success
- `400 Bad Request` - Missing or invalid a required path identifier
- `404 Not Found` - Currency not found in the database
- `500 Internal Server Error` - Error (e.g., database unavailable)

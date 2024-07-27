# 💱 Конвертер Валют

| 🚀 Добро пожаловать в проект "Конвертер валют"! Этот пет-проект не только о конвертации валют — это мое путешествие в мир Java сервлетов, позволяющее увлекательно и эффективно учиться.<br/>💹 Проект предлагает набор API для реального взаимодействия с валютами и обменными курсами, делая его функциональным и образовательным. | ![](/.github/img/currency-converter-main.webp) |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------|

### 🚀 Демонстрация
`Страница проекта:` [Приложение конвертера](https://converter.ale-os.com).

### 🚀 Доступ к API
`Получить доступ к открытому API по следующему адресу (http, чувствительно к регистру):` http://ale-os.com:9091/currency-converter/exchangeRates

### 📚 Узнать больше
`Если вы заинтересованы в изучении сервлетов, вы можете найти мою статью полезной, доступна здесь:` [Читать о сервлетах](https://ale-os.com/?s=%D1%81%D0%B5%D1%80%D0%B2%D0%BB%D0%B5%D1%82) (в процессе)

## 📖 Оглавление 

💡 [Размышления о проекте](#архитектурный-подход)
  - 🛠 [о подходе](#архитектурный-подход)
  - 📁 [о дао](#о-дао)
  - 🛠  [о сервисах](#о-сервисах)
  - 🌐  [о сервлетах](#о-сервлетах)
  - 🔍  [о фильтрах](#о-фильтрах)
  - 🚀 [о развертывании](#о-развертывании)

📉 [Exchange API](#exchange-api)

💱 [Currency API](#currency-api)

💹 [Exchange Rate API](#exchange-rate-api)

📋 [Требования к проекту](https://zhukovsd.github.io/java-backend-learning-course/projects/currency-exchange/)

🌱 [Установка](#установка)

💬 [Поделитесь своим мнением](#поделитесь-своим-мнением)

🙌 [Благодарности](#благодарности)

## 🎯 Цель проекта
Цель этого проекта — научиться обрабатывать HTTP-запросы с акцентом на использовании API сервлетов. Основные инструменты, которые используются в проекте:

- **🌐 Servlet API**:
- **🐱 Tomcat**
- **🚀 FlywayDB**
- **💾 SQLite**
- **🌊 HikariCP**
- **🔄 ModelMapper**
- **📊 GSON**
- **🛠 Maven**

## Архитектурный подход💡

#### Инициализация с [ApplicationStartupListener](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/listener/ApplicationStartupListener.java):
`ApplicationStartupListener используется для инициализации контекста с необходимыми компонентами.`

`Часть компонентов перечислено в виде элементов поля класса (LinkedHashSet). Вспомогательный класс ComponentInitializerUtil будет использован для создания этих компонентов. Объекты базы данных и маппера вынесены отдельно и полагаются на персональные методы, из-за сложности процесса инициализации`

`ApplicationStartupListener также управляет регистрацией сервлетов, нацеленных на динамическую настройку (маппинг через файл конфигурации), которая не может быть достигнута через аннотации или web.xml.`

#### Созданией компонентов через [ComponentInitializerUtil](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/util/ComponentInitializerUtil.java):
`ComponentInitializerUtil использует рефлексию для создания объектов. Зависимости определяются с помощью RequestAttributeUtil (определение стандартного имени компонентов) и ServletContext'а (контейнер где эти зависимости хранятся.)`

#### Конфигурация базы данных с [DatabaseUtil](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/util/DatabaseUtil.java):
`DatabaseUtil создает экземпляр базы данных SQLite в памяти. Используя при этом  HikariCP для эффективного пулинга соединений и Flyway для управления эволюцией схемы базы данных.`

#### Управление свойствами с [PropertiesUtil](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/util/PropertiesUtil.java):
`PropertiesUtil выступает в роли поставщика данных конфигурации, загружая их из файла application.properties.`

### О дао💡

Несмотря на то, что спецификация проекта не требует всех операций CRUD, я решил их реализовать, чтобы получить полный опыт взаимодействия сущностей через абстрактный класс CRUD. Вынесение общей логики в базовый класс, помогло улучшить повторное использование кода и его поддержку.
[link](https://github.com/aleos-dev/currency-converter/tree/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/dao)

#### [Управление несколькими идентификаторами](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/dao/CurrencyDao.java):
`В этой реализации присутствуют как суррогатные, так и натуральные ключи, которые используются в качестве идентификаторов сущностей. Однако это 
приводит к проблемам со сложностью их поддержки, назовем это "двойственностью идентификаторов". Это вызывает двоякие чувства, поэтому этот вопрос следует учитывать в будущих реализациях.`

#### [Абстрактный класс для операций CRUD](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/dao/CrudDao.java):
`Для централизации общих функций CRUD был использован абстрактный класс, обязывающий все сущности реализовать общий интерфейс с методами getId() и setId(). Этот дизайнерский выбор позволил абстрагировать операции CRUD, связанные с идентификатором сущности, в родительский класс.`

#### Операции с базой данных: сложность против эффективности
`Все в одном SQL-запросе: Для минимизации сложности на уровне приложения, я значительно увеличил сложность SQL-выражений. Этот подход, продемонстрированный в запросу `[SELECT_CROSS_RATE_BY_CODES](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/dao/ConversionRateDao.java#L68)`, включает сложные SQL-конструкции, такие как подзапросы и объединения, чтобы инкапсулировать сложную бизнес-логику внутри базы данных. Хотя это снижает потребность в дополнительной логике приложения для обработки данных, это делает SQL-выражения более сложными и потенциально трудными для поддержки.`

`Управление транзакциями: Реализация нескольких операций в рамках одной транзакции в методе `[saveAndFetch](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/dao/ConversionRateDao.java#L143)` подчеркнула необходимость надежного обработчика ошибок и эффективного управления транзакциями. Этот подход обеспечивает целостность и согласованность данных, но требует тщательного управления границами транзакций и сценариями ошибок. Он включает тщательный дизайн для эффективного управления откатами и исключениями, тем самым защищая от аномалий данных и операционных сбоев.`

`Что будет лучше?: Не всегда очевидно, какой подход — простой SQL с большей логикой на уровне приложения или сложный SQL с минимальной обработкой на уровне приложения — приведет к лучшей производительности. Эта неопределенность приводит к необходимости компромисса.`

#### Периодические ошибки SQLite:
`Во время разработки я столкнулся с эпизодическими ошибками, связанными с исчезновением таблиц. Эта проблема оказалась уклончивой и остается нерешенной.`

###### Решено: Оказывается, HikariCP закрывает неактивные соединения, что приводит к потере базы данных SQLite в памяти. Следовательно, когда HikariCP пытается подключиться по требованию, она создает новую базу данных в памяти без миграций. Эта проблема была решена путем установки свойства minimumIdle HikariCP равным 1.

#### Булевой тип результата в операциях CRUD:
`Методы CRUD были разработаны для возврата булева значения, указывающего на успех или неудачу. Такой двоичный индикатор может оказаться недостаточным в ситуациях, где требуется подробная обратная связь.`

### О сервисах💡

Сервисный слой в этом приложении в основном повторяет функциональность слоя DAO из-за простоты требований к приложению. Этот слой обрабатывает как DTO, так и сущности, используя экземпляр ModelMapper для преобразований между этими типами.[link](https://github.com/aleos-dev/currency-converter/tree/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/service)

#### Проблемы интеграции:
`Сервисный слой выявил некоторые сложности, связанные с взаимодействием между объектами передачи данных (DTO) и объектами доступа к данным (DAO), где требуемые интерфейсы не всегда совместимы.`[link](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/service/ConversionRateService.java#L21)

`Например, в то время как DTO курса конверсии может включать только натуральные ключи для идентификации валют, соответствующая сущность требует полных экземпляров валют.`

`Это несоответствие означает, что методы, такие как save(ConversionRate) или update(ConversionRate), не могут быть использованы напрямую без предварительной конструкции полных объектов валют.`

`Для решения этой проблемы необходимо создать новые методы DAO, которые отвечают этим требованиям, либо реализовать обходные решения, оба из которых могут рассматриваться как менее идеальные. Этот сценарий представляет значительное дизайнерское решение: следует ли адаптировать API для включения более удобных методов, или следует переложить ответственность на клиентов за управление сложностями предоставления того, что требует DAO?`

#### Сложность в преобразовании ModelMapper:
`Использование ModelMapper выявило ограничения при работе с record'ами в Java из-за его зависимости от рефлексии, которая не оптимизирована для неизменяемых свойств record'ов. Это привело к ручной настройке сопоставлений DTO-сущностей.`[link](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/mapper/ConversionRateMapper.java#L18)

#### Механизм кэширования:
`Реализация CacheService была предназначена для моделирования поведения слоя кэширования. Она предоставляет информацию о операциях кэширования через консольные логи при включении. Этот подход с кеширование служит только образовательным целям.`

### О сервлетах💡
Я стараюсь держать этот слой максимально простым. Ответственность по валидации, извлечении данных и составлении ответов переложены на фильтры. Сервлет вызывает сервис и обрабатывает результат должным образом.[link](https://github.com/aleos-dev/currency-converter/tree/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/servlet)

#### BaseServlet:
`Все основные сервлеты наследуют абстрактный BaseServlet, который переопределяет стандартный метод init() для внедрения зависимостей.`[link](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/servlet/BaseServlet.java#L16)

#### CustomDefaultServlet:
`Это настраиваемая версия DefaultServlet. DefaultServlet предназначен для обслуживания статических файлов и действует как запасной вариант, когда контейнер сервлетов не может найти подходящее сопоставление для текущего запроса. Обычно он обрабатывает методы GET и HEAD, предназначенные для получения ресурсов.`[link](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/servlet/common/CustomDefaultServlet.java)

`Однако были обнаружены неожиданные коды ошибок во время запросов на несуществующие ресурсы: 405 (PUT, POST и т.д.), 404 (GET, HEAD) и 501 (PATCH) методы.`

`Для приведения этого поведения в соответствие с типичными ожиданиями — где несуществующие ресурсы должны приводить к ответу 404 Not Found — поведение DefaultServlet было переопределено в CustomDefaultServlet. Это может быть спорным, но было сделано в образовательных целях. Так что, никакого вреда.`

`Также можно переопределить сопоставление по умолчанию для DefaulServlet, указывающее на root директорию , на более специфическое, такое как "/static/*". `

#### Обработка ошибок:
Возможности сервлет-контейнера можно использовать для обработки кодов ошибок. Существует два основных варианта для обработки ошибок, конфигурация которых возможна через web.xml:

[Ошибка 404](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/servlet/common/Error404Servlet.java#L14)`: Это обрабатывается сервлетом Error404Servlet, который предоставляет настраиваемый JSON-ответ с юмористическим сообщением.`

[Ошибка 405](https://github.com/aleos-dev/currency-converter/blob/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/webapp/error405.jsp#L4)`: Это управляется с помощью статического ресурса, error405.jsp, чтобы информировать пользователей о том, что метод не разрешен.`

### О фильтрах💡
Понимание роли фильтров в разработке веб-приложений имеет решающее значение. Фильтры делают гораздо больше, чем просто изменяют входящие запросы — они необходимы для управления различными аспектами обработки запросов.

#### Общие фильтры 
Эти фильтры обрабатывают фундаментальные операции, которые критичны для инфраструктуры приложения. [link](https://github.com/aleos-dev/currency-converter/tree/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/filter/common)

**EncodingFilter**:
`Этот фильтр устанавливает кодировку символов, используемую в теле запроса и ответа.`

**ExceptionHandlingFilter**:
`Этот фильтр перехватывает исключения, возникающие во время выполнения программы и не поддающиеся решению на месте. Он формирует соответствующий ответ клиенту, гарантируя, что необработанные исключения не приведут к сбою приложения.`

**JsonResponseFilter**:
`Применяется после обработки сервлетом, этот фильтр отвечает за запись ответа в формате JSON, если в RequestContext содержится объект ответа под указанным атрибутом.`

**CorsFilter**:
`Cross-Origin Resource Sharing (CORS) — это механизм, основанный на HTTP-заголовках, который позволяет серверу указать, какие источники (домен, схема или порт), отличные от своих, могут получать доступ к его ресурсам. Этот фильтр устанавливает соответствующие заголовки, чтобы включить бэкенд для приема запросов от клиентов с других доменов.`

**CachingFilter**:
`Этот фильтр проверяет, доступен ли кэшированный ответ для запроса. Если да, он возвращает кэшированный ответ; если нет, он переходит к следующему фильтру. После обработки запроса сервлетом ответ сохраняется в кэше. Фильтр также инвалидирует текущее состояние кэша для методов, изменяющих данные, таких как POST или PATCH. Для кэширования используется CacheService, хотя его можно было бы упростить, используя ServletContext для хранения записей кэша.`

#### Специфические для сервлетов фильтры:
Эти фильтры привязаны к конкретным сервлетам и готовят рабочую среду для них. Их основная функция заключается в извлечении и валидации объектов полезной нагрузки с помощью внедренных валидаторов.[link](https://github.com/aleos-dev/currency-converter/tree/26341292fbc3e63f743f73f9f96e54997d24ecf6/backend/src/main/java/com/aleos/filter/url)

    CurrenciesUrlFilter
    CurrencyUrlFilter
    ConversionRateUrlFilter
    ConversionUrlFilter

Каждый фильтр гарантирует, что входящие данные запроса извлечены и проверены валидатором перед их обработкой сервлетами. Они регистрируются с использованием web.xml, так как @WebFilter не может гарантировать порядок.

### О развертывании🚀
`Docker упрощает запуск моего приложения, особенно тогда, когда оно состоит из нексольких частей и я ценю его удобство. Этот энтузиазм побудил меня упаковать мое приложение в Docker Hub. Затем я загрузил образы на свой сервер и запустил Docker Compose. Изначально все казалось нормальным, но со временем сервер начал испытывать проблемы и в итоге завис, что потребовало перезагрузки. После расследования я обнаружил, что 1 ГБ оперативной памяти на моем сервере недостаточно для Docker, так как он интенсивно работает с памятью.`

`В результате я вернулся к использованию моего ранее работающего сервера Apache, на котором работал WordPress. Не зная, как обслуживать WAR-файл на Apache, я решил запустить Tomcat в фоновом режиме. Затем я настроил Apache только для обработки фронтенда, перенаправляя его на localhost. Я также настроил Apache для проксирования запросов на бэкенд через SSL-сертификаты, избавив себя от необходимости настройки SSL на Tomcat. Наконец, я открыл бэкенд на порту 9091 (http).`

Примечание:
`Во время исследования проблемы с избыточным потреблением ресурсов Docker'ом, я обнаружил, что MySQL, который поддерживает сайт WordPress (содержащий около 10 КБ контента), потребляет примерно 400 МБ оперативной памяти. Это чрезмерное использование было связано с функцией, известной как "Performance Schema", которая предоставляет подробную диагностику в реальном времени. Учитывая, что мой WordPress является минимальной настройкой для личного использования, я отключил эту функцию. Я достиг этого, установив performance_schema = 0 в файле /etc/mysql/mysql.conf.d/mysqld.conf. После этой корректировки потребление памяти упало более чем наполовину. (до 170М)`

## Установка🌱

Следуйте этим шагам, чтобы запустить проект на вашем локальном компьютере.

### Предварительные условия

Перед началом убедитесь, что у вас установлено и доступно следующее программное обеспечение:

- **Docker:** [Скачать Docker](https://www.docker.com/products/docker-desktop)
- **Docker Compose:** Включен в Docker Desktop или [Установить Docker Compose](https://docs.docker.com/compose/install/)

Убедитесь, что порты **9091** и **80** на localhost свободны для использования.

### Установка

1. **Скачайте и распакуйте проект:**

- Скачайте ZIP-файл проекта из репозитория.

- Распакуйте файл в желаемую директорию.

2. **Перейдите в директорию проекта:**

    ```sh
    cd yourprojectdirectory
    ```

### Запуск проекта

Используйте Docker Compose для сборки и запуска проекта в отсоединенном режиме.

1. **Соберите и запустите Docker-контейнеры:**

    ```sh
    docker-compose up --build -d
    ```

### Основное использование

- **Доступ к фронтенду:**

  Когда проект запущен, вы можете получить доступ к фронтенду по адресу:

    ```sh
    http://localhost:80
    ```

- **Доступ к API:**

  Вы можете получить доступ к API по адресу:

    ```sh
    http://localhost:9091/currency-converter
    ```

# 🔗 API

This API allows you to interact with exchange rates and currencies. Below are the available endpoints and their usage.

#### 💹 Exchange Rate API
- [x] [GET All Exchange Rates](#get-all-exchange-rates)
- [x] [GET Specific Exchange Rate](#get-specific-exchange-rates)
- [x] [Add New Exchange Rate](#add-new-exchange-rate)
- [x] [Update Existing Exchange Rate](#update-existing-exchange-rate)
- [x] [Delete Existing Exchange Rate](#delete-existing-exchange-rate)

#### 📉 Exchange API
- [x] [Currency Exchange Calculation](#currency-exchange-calculation)

#### 💱 Currency API
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
### 💬 Поделитесь своим мнением
Я постоянно стремлюсь улучшить свое понимание и реализацию программирования. Если у вас есть идеи,
критика или советы — или если вы хотите обсудить любой аспект этого проекта подробнее — я тепло приветствую ваш
вклад. Пожалуйста, не стесняйтесь [открыть вопрос](https://github.com/aleos-dev/currency-converter/issues), чтобы поделиться
своими
мыслями.

## Благодарности🙌

`Я хочу выразить свою благодарность автору `[технических требований](https://zhukovsd.github.io/java-backend-learning-course/projects/currency-exchange/)` для этого проекта, `[Жукову С. Д.](https://t.me/zhukovsd_it_mentor)`, и `[сообществу](https://t.me/zhukovsd_it_chat)`, которое он собрал вокруг себя. Быть учасником которого приятно и полезно.`

`Особая благодарность Java за то, что мне не пришлось программировать это на ассемблере!`

`И большой привет для кофе – топливу, которое помогло этому проекту гладко функционировать!`

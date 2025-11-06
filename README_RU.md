# OpenWeatherMap SDK

SDK для работы с OpenWeatherMap API, поддерживающий два режима работы: ON_DEMAND и POLLING.

## Установка

### Maven

Добавьте зависимость в ваш `pom.xml`:

```xml
<dependency>
  <groupId>ru.sterkhovkv</groupId>
  <artifactId>open-weather-map</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Требования

- Java 21 или выше
- Maven 3.6+ (для сборки проекта)

## Быстрый старт

### Получение экземпляра SDK

```java
import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;

// Получение SDK с API ключом
OpenWeatherMapSDK sdk = SDKFactory.getInstance("your-api-key", SDKMode.ON_DEMAND);

// Или с кастомной конфигурацией
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;

SDKConfig config = SDKConfig.builder()
        .apiVersion(ApiVersion.V2_5)
        .cacheSize(20)
        .cacheTtlMinutes(15)
        .build();

OpenWeatherMapSDK sdk = SDKFactory.getInstance("your-api-key", SDKMode.ON_DEMAND, config);
```

**Важно:** API ключ можно передать напрямую или установить в переменную окружения `OPENWEATHER_API_KEY`. Если ключ не передан, SDK попытается использовать значение из переменной окружения.

### Получение погоды

```java
import ru.sterkhovkv.openweathermap.model.WeatherResponse;

WeatherResponse weather = sdk.getWeather("Moscow");
System.out.println("Temperature: " + weather.getTemperature().getTemp() + "K");
        System.out.println("Weather: " + weather.getWeather().getDescription());
```

**Примечание:** При поиске города по имени SDK возвращает информацию о **первом найденном городе** из результатов поиска OpenWeatherMap API.

### Очистка ресурсов

```java
// Уничтожение конкретного экземпляра
sdk.destroy();

// Или через фабрику (рекомендуется)
SDKFactory.removeInstance("your-api-key");

// Уничтожение всех экземпляров
SDKFactory.removeAllInstances();
```

## Режимы работы

### ON_DEMAND

Данные запрашиваются только при вызове `getWeather()`. Если данные есть в кэше и еще актуальны (не истек TTL), возвращаются из кэша без запроса к API.

```java
OpenWeatherMapSDK sdk = SDKFactory.getInstance("api-key", SDKMode.ON_DEMAND, config);
WeatherResponse weather = sdk.getWeather("Moscow"); // Запрос к API, если нет в кэше
WeatherResponse cached = sdk.getWeather("Moscow");   // Возврат из кэша (если TTL не истек)
```

### POLLING

Данные автоматически обновляются в фоне для всех городов в кэше через заданные интервалы. Метод `getWeather()` всегда возвращает данные из кэша (нулевая задержка).

```java
OpenWeatherMapSDK sdk = SDKFactory.getInstance("api-key", SDKMode.POLLING, config);
sdk.getWeather("Moscow"); // Добавляет город в кэш и запускает фоновое обновление
// Планировщик автоматически обновит данные через pollingIntervalMinutes
// Все последующие вызовы getWeather() возвращают данные из кэша мгновенно
```

## Работа с несколькими API ключами

SDK поддерживает работу с несколькими API ключами одновременно. Для каждого уникального API ключа создается отдельный экземпляр SDK. Попытка создать второй экземпляр с тем же API ключом вернет существующий экземпляр (если режим совпадает) или выбросит исключение `IllegalSDKStateException` (если режим отличается).

```java
// Создание экземпляров с разными API ключами
OpenWeatherMapSDK sdk1 = SDKFactory.getInstance("api-key-1", SDKMode.ON_DEMAND);
OpenWeatherMapSDK sdk2 = SDKFactory.getInstance("api-key-2", SDKMode.ON_DEMAND);

// Попытка создать второй экземпляр с тем же ключом
OpenWeatherMapSDK sdk3 = SDKFactory.getInstance("api-key-1", SDKMode.ON_DEMAND);
// sdk3 == sdk1 (возвращается существующий экземпляр)

// Попытка создать экземпляр с тем же ключом, но другим режимом
OpenWeatherMapSDK sdk4 = SDKFactory.getInstance("api-key-1", SDKMode.POLLING);
// Выбросит IllegalSDKStateException

// Удаление экземпляра по API ключу
SDKFactory.removeInstance("api-key-1");

// Проверка существования экземпляра
boolean exists = SDKFactory.hasInstance("api-key-1");

// Получение количества активных экземпляров
int count = SDKFactory.getInstanceCount();
```

## Структура ответа

SDK возвращает объект `WeatherResponse` со следующей структурой (соответствует требованиям ТЗ):

```json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 269.6,
    "feelsLike": 267.57
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "Zocca"
}
```

### Описание полей

- `weather` - информация о погодных условиях
  - `main` - основное описание (например, "Clouds", "Clear")
  - `description` - детальное описание
- `temperature` - информация о температуре
  - `temp` - температура (по умолчанию в Кельвинах)
  - `feelsLike` - ощущаемая температура
- `visibility` - видимость в метрах
- `wind` - информация о ветре
  - `speed` - скорость ветра (по умолчанию в м/с)
- `datetime` - текущее время в формате Unix timestamp (UTC)
- `sys` - системная информация
  - `sunrise` - время восхода солнца (Unix timestamp)
  - `sunset` - время захода солнца (Unix timestamp)
- `timezone` - смещение часового пояса в секундах от UTC
- `name` - название города

## Конфигурация

### SDKConfig

```java
SDKConfig config = SDKConfig.builder()
        .apiVersion(ApiVersion.V3_0)              // V3_0 (по умолчанию) или V2_5
        .maxCallsPerDay(2000)                     // Максимум запросов в день
        .maxCallsPerMinute(60)                    // Максимум запросов в минуту
        .requestTimeoutSeconds(30)                // Таймаут запроса
        .connectTimeoutSeconds(10)                // Таймаут подключения
        .cacheSize(10)                            // Размер кэша (количество городов, по умолчанию 10)
        .cacheTtlMinutes(10)                      // TTL кэша в минутах (по умолчанию 10)
        .pollingIntervalMinutes(10)               // Интервал обновления в POLLING режиме
        .pollingStrategy(PollingStrategy.STRICT)  // Стратегия обновления
        .preemptiveEpsilonMinutes(1)             // Эпсилон для PREEMPTIVE_EPSILON стратегии
        .units(TemperatureUnits.METRIC)           // Единицы измерения (STANDARD, METRIC, IMPERIAL)
        .lang("ru")                               // Язык описаний погоды
        .build();
```

**Значения по умолчанию (соответствуют требованиям ТЗ):**
- `cacheSize`: 10 городов (максимум)
- `cacheTtlMinutes`: 10 минут (данные считаются актуальными, если прошло менее 10 минут)

### Версии API

- **V2_5** - Current Weather Data API 2.5 (совместимо со всеми API ключами)
- **V3_0** - One Call API 3.0 (требует подписку "One Call by Call", используется по умолчанию)

### Стратегии обновления (PollingStrategy)

- **STRICT** - обновлять все города каждый тик (по умолчанию)
- **PREEMPTIVE_EPSILON** - обновлять города, у которых TTL истекает в течение epsilon минут

## Обработка исключений

Все методы SDK выбрасывают исключения с описанием причины ошибки:

```java
try {
WeatherResponse weather = sdk.getWeather("Moscow");
} catch (IllegalArgumentException e) {
        // Неверные параметры (например, null или пустая строка для имени города)
        System.err.println("Invalid argument: " + e.getMessage());
        } catch (IllegalSDKStateException e) {
        // SDK был уничтожен
        System.err.println("SDK destroyed: " + e.getMessage());
        } catch (CityNotFoundException e) {
        // Город не найден
        System.err.println("City not found: " + e.getMessage());
        } catch (InvalidApiKeyException e) {
        // Неверный API ключ
        System.err.println("Invalid API key: " + e.getMessage());
        } catch (ApiRateLimitException e) {
        // Превышен лимит запросов
        System.err.println("Rate limit exceeded: " + e.getMessage());
        } catch (NetworkException e) {
        // Ошибка сети
        System.err.println("Network error: " + e.getMessage());
        } catch (BadRequestException e) {
        // Неверный запрос
        System.err.println("Bad request: " + e.getMessage());
        e.getInvalidParameters(); // Список неверных параметров
} catch (SDKException e) {
        // Общая ошибка SDK
        System.err.println("SDK error: " + e.getMessage());
        }
```

## Примеры использования

### Пример 1: Базовое использование в режиме ON_DEMAND

```java
import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;
import ru.sterkhovkv.openweathermap.model.WeatherResponse;

public class BasicExample {
  public static void main(String[] args) {
    String apiKey = "your-api-key-here";

    // Создание конфигурации
    SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .build();

    // Получение экземпляра SDK
    OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND, config);

    try {
      // Получение погоды для одного города
      WeatherResponse weather = sdk.getWeather("Moscow");

      System.out.println("City: " + weather.getName());
      System.out.println("Temperature: " + weather.getTemperature().getTemp() + "K");
      System.out.println("Feels like: " + weather.getTemperature().getFeelsLike() + "K");
      System.out.println("Weather: " + weather.getWeather().getMain() +
              " - " + weather.getWeather().getDescription());
      System.out.println("Wind speed: " + weather.getWind().getSpeed() + " m/s");
      System.out.println("Visibility: " + weather.getVisibility() + " m");

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      // Очистка ресурсов
      SDKFactory.removeInstance(apiKey);
    }
  }
}
```

### Пример 2: Использование кэша

```java
public class CacheExample {
  public static void main(String[] args) {
    String apiKey = "your-api-key-here";
    OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

    try {
      // Первый запрос - данные загружаются из API
      long start1 = System.currentTimeMillis();
      WeatherResponse weather1 = sdk.getWeather("London");
      long time1 = System.currentTimeMillis() - start1;
      System.out.println("First request (from API): " + time1 + " ms");

      // Второй запрос - данные возвращаются из кэша (быстрее)
      long start2 = System.currentTimeMillis();
      WeatherResponse weather2 = sdk.getWeather("London");
      long time2 = System.currentTimeMillis() - start2;
      System.out.println("Second request (from cache): " + time2 + " ms");

      System.out.println("Cache size: " + sdk.getCacheSize());

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    } finally {
      SDKFactory.removeInstance(apiKey);
    }
  }
}
```

### Пример 3: Работа с несколькими городами

```java
public class MultipleCitiesExample {
  public static void main(String[] args) {
    String apiKey = "your-api-key-here";
    OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

    try {
      String[] cities = {"Paris", "Tokyo", "New York"};

      for (String city : cities) {
        try {
          WeatherResponse weather = sdk.getWeather(city);
          System.out.println(city + ": " +
                  weather.getTemperature().getTemp() + "K, " +
                  weather.getWeather().getMain());
        } catch (Exception e) {
          System.err.println("Failed for " + city + ": " + e.getMessage());
        }
      }

      System.out.println("Total cities in cache: " + sdk.getCacheSize());

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    } finally {
      SDKFactory.removeInstance(apiKey);
    }
  }
}
```

### Пример 4: Режим POLLING

```java
public class PollingExample {
  public static void main(String[] args) {
    String apiKey = "your-api-key-here";

    SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .pollingIntervalMinutes(10)  // Обновление каждые 10 минут
            .build();

    OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.POLLING, config);

    try {
      // Добавляем города в кэш
      System.out.println("Adding cities to cache...");
      sdk.getWeather("Berlin");
      sdk.getWeather("Madrid");
      System.out.println("Cache size: " + sdk.getCacheSize());

      // Планировщик автоматически обновит данные в фоне
      // Все последующие запросы возвращают данные из кэша мгновенно
      System.out.println("Polling scheduler started. Data will be updated every 10 minutes.");

      // Получение данных из кэша (нулевая задержка)
      WeatherResponse weather = sdk.getWeather("Berlin");
      System.out.println("Berlin weather from cache: " +
              weather.getTemperature().getTemp() + "K");

      // Ждем некоторое время (в реальном приложении)
      Thread.sleep(5000);

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    } finally {
      // Остановка планировщика и очистка ресурсов
      SDKFactory.removeInstance(apiKey);
    }
  }
}
```

### Пример 5: Работа с несколькими API ключами

```java
public class MultipleApiKeysExample {
  public static void main(String[] args) {
    String apiKey1 = "first-api-key";
    String apiKey2 = "second-api-key";

    try {
      // Создание экземпляров с разными ключами
      OpenWeatherMapSDK sdk1 = SDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
      OpenWeatherMapSDK sdk2 = SDKFactory.getInstance(apiKey2, SDKMode.ON_DEMAND);

      // Использование разных экземпляров
      WeatherResponse weather1 = sdk1.getWeather("Moscow");
      WeatherResponse weather2 = sdk2.getWeather("London");

      System.out.println("Active SDK instances: " + SDKFactory.getInstanceCount());

      // Попытка создать дубликат с тем же ключом вернет существующий экземпляр
      OpenWeatherMapSDK sdk1Copy = SDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
      System.out.println("sdk1 == sdk1Copy: " + (sdk1 == sdk1Copy)); // true

      // Удаление экземпляров
      SDKFactory.removeInstance(apiKey1);
      SDKFactory.removeInstance(apiKey2);

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}
```

### Пример 6: Обработка ошибок

```java
public class ErrorHandlingExample {
  public static void main(String[] args) {
    String apiKey = "your-api-key-here";
    OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

    try {
      // Попытка получить погоду для несуществующего города
      try {
        sdk.getWeather("NonExistentCity12345");
      } catch (CityNotFoundException e) {
        System.err.println("City not found: " + e.getMessage());
      }

      // Попытка передать null
      try {
        sdk.getWeather(null);
      } catch (IllegalArgumentException e) {
        System.err.println("Invalid argument: " + e.getMessage());
      }

    } catch (InvalidApiKeyException e) {
      System.err.println("Invalid API key: " + e.getMessage());
    } catch (ApiRateLimitException e) {
      System.err.println("Rate limit exceeded: " + e.getMessage());
    } catch (NetworkException e) {
      System.err.println("Network error: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("Unexpected error: " + e.getMessage());
    } finally {
      SDKFactory.removeInstance(apiKey);
    }
  }
}
```

## Дополнительная информация

### Кэширование данных о погоде

- SDK использует LRU (Least Recently Used) кэш для хранения данных о погоде
- Максимальный размер кэша: 10 городов (настраивается через `cacheSize` в `SDKConfig`)
- TTL кэша: 10 минут (настраивается через `cacheTtlMinutes` в `SDKConfig`)
- При достижении лимита кэша самый старый город удаляется из кэша
- Данные считаются актуальными, если с момента последнего обновления прошло менее установленного TTL

### Кэширование координат городов

SDK также использует внутренний кэш для координат городов (геокодинг):

- **Максимальный размер**: 100 городов
- **TTL**: 24 часа
- **Нормализация**: имена городов автоматически нормализуются (приводятся к нижнему регистру, удаляются лишние пробелы) для оптимизации кэширования
- **Преимущества**:
  - Снижение количества запросов к Geocoding API
  - Ускорение работы при повторных запросах для тех же городов
  - Экономия лимитов API

Например, запросы `"Moscow"`, `"MOSCOW"`, `"  Moscow  "` будут использовать один и тот же кэшированный результат.

### Другие особенности

- В режиме POLLING планировщик автоматически запускается при создании SDK
- Один экземпляр SDK создается на один API ключ (singleton pattern per API key)
- SDK автоматически обрабатывает rate limiting API

## Полные примеры

Полные рабочие примеры можно найти в файлах:
- `TestOnDemandSDK.java` - пример использования в режиме ON_DEMAND
- `TestPollingSDK.java` - пример использования в режиме POLLING
```

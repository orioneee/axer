# Axer Library

<div align="center">
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/Screenshot%202025-06-29%20at%2010.18.32%E2%80%AFPM.png" width="45%" style="margin: 10px;" />
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/Screenshot%202025-06-29%20at%2010.18.51%E2%80%AFPM.png" width="45%" style="margin: 10px;" />
  <br />
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/Screenshot%202025-06-29%20at%2010.19.17%E2%80%AFPM.png" width="45%" style="margin: 10px;" />
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/Screenshot%202025-06-29%20at%2010.20.30%E2%80%AFPM.png" width="45%" style="margin: 10px;" />
  <br />
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/Screenshot%202025-06-29%20at%2010.20.57%E2%80%AFPM.png" width="45%" style="margin: 10px;" />
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/Screenshot%202025-06-29%20at%2010.25.15%E2%80%AFPM.png" width="45%" style="margin: 10px;" />
</div>



Axer is a library designed to monitor HTTP requests, record exceptions (fatal and non-fatal), and inspect Room databases in real-time with the ability to execute custom queries and edit values in table. It is inspired by the [Chucker](https://github.com/ChuckerTeam/chucker) library but provides additional features like Room database inspection.

**Note**: Axer is currently in alpha (`{{AXER_VERSION}}`) and is not considered stable. It supports Android, JVM, and iOS platforms. However, iOS has limitations: stack traces and fatal crash capturing via `installAxerErrorHandler` are not supported.

## Installation

Add the following dependencies to your project:

```kotlin
implementation("io.github.orioneee:axer:{{AXER_VERSION}}")
```

For production environments where monitoring is not needed, use the no-op variant to avoid code changes:

```kotlin
implementation("io.github.orioneee:axer-no-op:{{AXER_VERSION}}")
```

The no-op variant does nothing but maintains the same API, ensuring seamless integration in production.

## Initialization

You must initialize Axer **once** before any recording or monitoring on each platform. This is typically done in the `Application` class for Android, or in the main function for JVM applications.

Also don't forget about permissions for notification on android and ios

### Android

In your `Application` class:

```kotlin
class SampleApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    Axer.initialize(this)
  }
}
```

## Usage

### HTTP Request Monitoring

Axer can monitor HTTP requests and extract important data using selectors. It supports both Ktor and OkHttp clients.

#### Ktor Example

```kotlin
val client = HttpClient {
  install(DefaultRequest) {
    contentType(ContentType.Application.Json)
    if (!headers.contains("Authorization")) {
      header("Authorization", "Bearer your_token_here")
    }
  }
  install(Axer.ktorPlugin) {
    requestImportantSelector = { request: Request ->
      val tokenData = request.headers.entries.firstOrNull {
        it.key.equals("Authorization", ignoreCase = true)
      }
      if (tokenData != null) {
        listOf(tokenData.value)
      } else {
        emptyList()
      }
    }
    responseImportantSelector = {
      listOf("Some important response data")
    }
  }
}
```

#### OkHttp Example

```kotlin
AxerOkhttpInterceptor.Builder()
  .setRequestImportantSelector {
    if (it.headers.contains("cookie")) {
      val token = it.headers["cookie"]?.replace("PHPSESSID=", "")
      if (token.isNullOrBlank()) {
        emptyList()
      } else {
        listOf("PHPSESID: $token")
      }
    } else {
      emptyList()
    }
  }
  .build()
```

#### JVM Window

To display the Axer UI in a JVM environment:

```kotlin
fun main() = application {
  AxerWindows()
  ...
}
```

#### Call UI

To open the Axer UI programmatically:

```kotlin
Axer.openAxerUI()
```

#UI integration

if you want to integrate Axer UI in your app, you can use the following code:

```kotlin
AxerUIEntryPoint().Screen()
```

These configurations allow Axer to record network requests and display important data extracted by the provided selectors, which can be viewed in the UI. Also you can acces ui in android, ios by clicking on notification(**requare notification permissions**)

### Exception Handling

Axer can capture fatal crashes and manually recorded exceptions.

#### Fatal Crash Handling

Install the error handler on the main thread:

```kotlin
Axer.installAxerErrorHandler()
```

You can customize the crash at jvm and android by overriding the open class AxerUncaughtExceptionHandler and settings it like:

```kotlin
open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
  // Custom implementation
}
...
Thread.setDefaultUncaughtExceptionHandler(MyUncaughtExceptionHandler())

```

**Note**: Fatal crash handling on IOS may work incorrectly

#### Manual Exception Recording

Record exceptions manually:

```kotlin
Axer.recordException(Exception("Test exception"))
Axer.recordAsFatal(Exception("Test fatal exception"))
```

### Room Database Inspection

Axer supports live inspection of Room databases and execution of custom queries. Only one database can be monitored at a time.

#### Example Configuration

```kotlin
builder
  .setDriver(AxerBundledSQLiteDriver.getInstance())
  .setQueryCoroutineContext(Dispatchers.IO)
  .fallbackToDestructiveMigration(false)
  .build()
```

The only required configuration is setting the driver:

```kotlin
.setDriver(AxerBundledSQLiteDriver.getInstance())
```

## Limitations

- **Single Database Monitoring**: Axer can only monitor one Room database at a time.
- **iOS Limitations**:
  - Stack traces are not supported.
  - Fatal crash capturing via `installAxerErrorHandler` may work incorrectly.
- **Stability**: The library is in alpha (`{{AXER_VERSION}}`) and may have bugs or breaking changes in future releases.

## Inspiration

Axer was inspired by the [Chucker](https://github.com/ChuckerTeam/chucker) library, extending its capabilities with Room database inspection and cross-platform support.

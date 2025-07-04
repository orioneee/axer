<div align="center">
  <picture>
    <img width="200px" alt="Axer logo" src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/cropped_circle_image.png">
  </picture>
</div>

# Axer Library

<div align="center">
  <!-- GIFs: 4 in one line -->
  <div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px;">
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/requests.gif" width="22%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/exceptions.gif" width="22%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/logs.gif" width="22%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/room.gif" width="22%" />
  </div>

  <!-- Screenshots: 2 per line -->
  <div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px; margin-top: 20px;">
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/request.png" width="45%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/exception.png" width="45%" />
  </div>
  <div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px; margin-top: 10px;">
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/logs_ui.png" width="45%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/db_list.png" width="45%" />
  </div>
  <div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px; margin-top: 10px;">
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/db_view.png" width="45%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/db_query.png" width="45%" />
  </div>
</div>



Axer is a library designed to monitor **HTTP requests**, record **exceptions** (fatal and non-fatal), **inspect Room** databases in real-time with the ability to execute custom queries and edit values in table. Also library provide **logging** functional. It is inspired by the [Chucker](https://github.com/ChuckerTeam/chucker) library but provides additional features like Room database inspection.


## Installation

Add the following dependencies to your project:

```kotlin
implementation("io.github.orioneee:axer:1.0.0-beta10")
```

For production environments where monitoring is not needed, use the no-op variant to avoid code changes:

```kotlin
implementation("io.github.orioneee:axer-no-op:1.0.0-beta10")
```

The no-op variant does nothing but maintains the same API, ensuring seamless integration in production.

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
        requestReducer = { request ->
            val redactedHeaders = request.headers.mapValues { (key, value) ->
                if (key.equals("Authorization", ignoreCase = true)) "Bearer ***" else value
            }
            request.copy(headers = redactedHeaders) // this will present in request but not in UI
        }

        responseReducer = { response ->
            response.copy(
                headers = response.headers.mapValues { (key, value) ->
                    if (key.equals("Content-Type", ignoreCase = true)) "application/json" else value
                }
            ) // this will change content type which display only in UI
        }

        requestImportantSelector = { request ->
            listOf("Authorization: ${request.headers["Authorization"] ?: "Not set"}") // if you want highlight any important data in request
        }

        responseImportantSelector = { response ->
            listOf("status: ${response.status}", "content-type: ${response.headers["Content-Type"]}")
        }
        retentionPeriodInSeconds = 60 * 60 * 1
    }
}
```

#### OkHttp Example

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        AxerOkhttpInterceptor.Builder()
            .setRequestReducer { request ->
                val redactedHeaders = request.headers.mapValues { (key, value) ->
                    if (key.equals("Authorization", ignoreCase = true)) "Bearer ***" else value
                }
                request.copy(headers = redactedHeaders)
            } // this will present in request but not in UI
            .setResponseReducer { response ->
                response.copy(
                    headers = response.headers.mapValues { (key, value) ->
                        if (key.equals("Content-Type", ignoreCase = true)) "application/json" else value
                    }
                )
            } // this will change content type which display only in UI
            .setRequestImportantSelector { request ->
                listOf("Authorization: ${request.headers["Authorization"] ?: "Not set"}")
            } // if you want highlight any important data in request
            .setResponseImportantSelector { response ->
                listOf("status: ${response.status}", "content-type: ${response.headers["Content-Type"]}")
            }
            .setRetentionTime(60 * 60 * 1)
            .build()
    )
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

#### UI integration

if you want to integrate Axer UI in your app, you can use the following code:

```kotlin
AxerUIEntryPoint().Screen()
```

These configurations allow Axer to record network requests and display important data extracted by the provided selectors, which can be viewed in the UI. Also you can acces ui in android, ios by clicking on notification(**requare notification permissions**)

### Logger
Logger maded by [Napier](https://github.com/AAkira/Napier)

**if you already use Napier you just need install logsaver**

```kotlin
Napier.base(AxerLogSaver())
```

and simply log with napier as before, all logs will be saved and displayed



**if you dont use Napier**
You need initialize logger, this will apply base to Napier
```kotlin
Axer.initializeLogger()
```
than you can log just like in android but call Axer
```kotlin
Axer.d("App", "Test debug log")
//Axer.d("App", "Test debug log with throwable", Throwable("Test throwable"))
Axer.e("App", "Test error log")
//Axer.e("App", "Test error log with throwable", Throwable("Test throwable"))
Axer.i("App", "Test info log")
//Axer.i("App", "Test info log with throwable", Throwable("Test throwable"))
Axer.w("App", "Test warning log")
//Axer.w("App", "Test warning log with throwable", Throwable("Test throwable"), record = false) // record = false will just log without saving to Axer
Axer.v("App", "Test verbose log")
//Axer.v("App", "Test verbose log with throwable", Throwable("Test throwable"))
Axer.wtf("App", "Test assert log")
Axer.wtf("App", "Test assert log with throwable", Throwable("Test throwable"))
```

<div style="display: flex; justify-content: center; margin-top: 20px;">
  <table style="border-collapse: collapse; text-align: center;">
    <tr>
      <td>
        <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/logs_ide.png" alt="Logs Screenshot" width="500px" style="margin: 10px;" />
        <div>In IDE</div>
      </td>
      <td>
        <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/logs.png" alt="IDE Logs Screenshot" width="500px" style="margin: 10px;" />
        <div>In Axer</div>
      </td>
    </tr>
  </table>
</div>


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

Axer supports live inspection of Room databases and execution of custom queries. Now multiple databases are supported.

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

## Configuration
You can configure available options for monitoring in runtime(by default all enabled)

```kotlin
Axer.configure {
    enableRequestMonitor = true
    enableExceptionMonitor = true
    enableLogMonitor = true
    enableDatabaseMonitor = true
}
```


**Stability**:

The library is in beta (`1.0.0-beta10`) and may have bugs or breaking changes in future releases.

## iOS Limitations
- Stack traces are not supported.
- Fatal crash capturing via `installAxerErrorHandler` currently not working.


## Inspiration

Axer was inspired by the [Chucker](https://github.com/ChuckerTeam/chucker) library, extending its capabilities with Room database inspection and cross-platform support.

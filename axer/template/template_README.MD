<div align="center">
  <picture>
    <img width="200px" alt="Axer logo" src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/logo.svg">
  </picture>
</div>

# Axer Library

---

## 📑 Summary

- [Overview](#overview)
- [🚀 Remote Debugger Support](#-remote-debugger-support)
- [Installation](#installation)
- [Features & Usage](#features--usage)
    - [HTTP Request Monitoring](#http-request-monitoring)
    - [Logger](#logger)
    - [Exception Handling](#exception-handling)
    - [Room Database Inspection](#room-database-inspection)
- [Runtime Configuration](#runtime-configuration)
- [Stability](#stability)
- [iOS Limitations](#ios-limitations)
- [Inspiration](#inspiration)
- [💡 Tips & Extras](#-tips--extras)
- [License](#license)

---

<div align="center">
  <!-- GIFs: 4 in one line -->
  <div style="display: flex; justify-content: center; flex-wrap: wrap; gap: 10px;">
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/requests_mobile.jpg" width="22%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/exceptions_mobile.jpg" width="22%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/logs_mobile.jpg" width="22%" />
    <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/db_mobile.jpg" width="22%" />
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

---

## Overview

**Axer** is a modern, multiplatform library for advanced runtime inspection and debugging in Kotlin/Android/JVM/iOS projects. It enables you to:

- Monitor **HTTP requests** with deep inspection and redaction capabilities (Ktor/OkHttp supported)
- Record and inspect **exceptions** (both fatal and non-fatal), and customize crash handling
- View and edit **Room databases** in real time, run custom queries, and manage multiple DBs
- Capture and display **logs**, powered by [Napier](https://github.com/AAkira/Napier)
- Use all these features locally or remotely via the **Remote Debugger** app

Axer is inspired by [Chucker](https://github.com/ChuckerTeam/chucker) and [KtorMonitor](http://github.com/CosminMihuMDC/KtorMonitor/), but adds multiplatform support and real-time database inspection.

---

## 🚀 Remote Debugger Support

Axer now features **remote debugging**! You can run the Axer debugger app on your desktop, discover debuggable apps (JVM or Android) on your local network, and inspect requests, exceptions, logs, databases, and more — all remotely.

> ⚡️ **Download the remote debugger app in the [Releases section](https://github.com/orioneee/Axer/releases).**  
> The standalone desktop debugger is distributed there.

<div align="center" style="margin-top: 20px;">
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/device_list.png" width="45%" />
  <img src="https://github.com/orioneee/Axer/raw/main/sample/screenshots/remote_debug.png" width="45%" />
</div>

### Enabling Remote Debugging

On your debuggable app (JVM/Android), manually start the Axer server:

```kotlin
CoroutineScope(Dispatchers.IO).launch {
    try {
        Axer.runServerIfNotRunning(this)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            this@SampleApplication,
            "Failed to start Axer server: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
```

- **Note**: Both debugger and debuggable device **must be on the same network**.
- **Only one server** can be runned on one device at same time (Axer uses port `53214`) so if you want monitor multiple app enshure only one server runned

---

## Installation

Add the dependencies to your project (`{{AXER_VERSION}}` is the latest version; check [Releases](https://github.com/orioneee/Axer/releases)):

```kotlin
implementation("io.github.orioneee:axer:{{AXER_VERSION}}")
```

For production, use the no-op variant to avoid runtime overhead and source changes:

```kotlin
implementation("io.github.orioneee:axer-no-op:{{AXER_VERSION}}")
```

No-op maintains the same API, so switching in/out is seamless.

---

## Features & Usage

### HTTP Request Monitoring

Axer monitors HTTP requests and highlights important data using customizable selectors.  
Supports **Ktor** and **OkHttp** out of the box.

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
            request.copy(headers = redactedHeaders)
        }
        responseReducer = { response ->
            response.copy(
                headers = response.headers.mapValues { (key, value) ->
                    if (key.equals("Content-Type", ignoreCase = true)) "application/json" else value
                }
            )
        }
        requestImportantSelector = { request ->
            listOf("Authorization: ${request.headers["Authorization"] ?: "Not set"}")
        }
        responseImportantSelector = { response ->
            listOf("status: ${response.status}", "content-type: ${response.headers["Content-Type"]}")
        }
        retentionPeriodInSeconds = 60 * 60 * 1
        retentionSizeInBytes = 10 * 1024 * 1024
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
            }
            .setResponseReducer { response ->
                response.copy(
                    headers = response.headers.mapValues { (key, value) ->
                        if (key.equals("Content-Type", ignoreCase = true)) "application/json" else value
                    }
                )
            }
            .setRequestImportantSelector { request ->
                listOf("Authorization: ${request.headers["Authorization"] ?: "Not set"}")
            }
            .setResponseImportantSelector { response ->
                listOf("status: ${response.status}", "content-type: ${response.headers["Content-Type"]}")
            }
            .setRetentionTime(60 * 60 * 1)
            .setRetentionSize(10 * 1024 * 1024)
            .build()
    )
    .build()
```

#### JVM Window

Display the Axer UI in your JVM app:

```kotlin
fun main() = application {
    AxerTrayWindow()
    ...
}
```

Or manually control the window:

```kotlin
fun main() = application {
    AxerWindows()
    ...
}
```

#### Call UI

Programmatically open the Axer UI:

```kotlin
Axer.openAxerUI()
```

#### UI Integration

Integrate Axer UI directly:

```kotlin
AxerUIEntryPoint().Screen()
```

Access the UI also from Android/iOS by clicking the notification (**requires notification permissions**).

---

### Logger

Axer logging is powered by [Napier](https://github.com/AAkira/Napier).

- **Already use Napier?**  
  Just install the Axer log saver:

  ```kotlin
  Napier.base(AxerLogSaver())
  ```

- **Don't use Napier?**  
  Use Axer logger directly:

  ```kotlin
  Axer.d("App", "Test debug log")
  Axer.e("App", "Test error log")
  Axer.i("App", "Test info log")
  Axer.w("App", "Test warning log")
  Axer.v("App", "Test verbose log")
  Axer.wtf("App", "Test assert log")
  ```

  > You can also log with exceptions and control saving with `record = false`.

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

---

### Exception Handling

Axer captures fatal crashes and manually recorded exceptions.

#### Fatal Crash Handling

Install the error handler on your main thread:

- **Android:**

  ```kotlin
  class MainActivity : ComponentActivity() {
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          Axer.installErrorHandler()
          ...
      }
  }
  ```

- **JVM:**

  ```kotlin
  fun main() = application {
      Axer.installErrorHandler()
      ...
  }
  ```

- **iOS:**

  ```kotlin
  fun MainViewController(): UIViewController {
      Axer.installErrorHandler()
      ...
  }
  ```

Customize crash handling by overriding `AxerUncaughtExceptionHandler`:

```kotlin
open class AxerUncaughtExceptionHandler : UncaughtExceptionHandler {
    // Custom implementation
}
...
Thread.setDefaultUncaughtExceptionHandler(MyUncaughtExceptionHandler())
```

#### Manual Exception Recording

Record exceptions manually:

```kotlin
Axer.recordException(Exception("Test exception"))
Axer.recordAsFatal(Exception("Test fatal exception"))
```

---

### Room Database Inspection

Axer supports live Room DB inspection and custom queries, with multiple DB support.

#### Example Configuration

```kotlin
builder
    .setDriver(AxerBundledSQLiteDriver.getInstance())
    .setQueryCoroutineContext(Dispatchers.IO)
    .fallbackToDestructiveMigration(false)
    .build()
```

Only required setup:

```kotlin
.setDriver(AxerBundledSQLiteDriver.getInstance())
```

---

## Runtime Configuration

Enable/disable monitoring options at runtime (all enabled by default):

```kotlin
Axer.configure {
    enableRequestMonitor = true
    enableExceptionMonitor = true
    enableLogMonitor = true
    enableDatabaseMonitor = true
}
```

---

## Stability

Axer is currently in beta (`{{AXER_VERSION}}`).  
Expect possible bugs and breaking changes as the library evolves.

---

## iOS Limitations

- Stack traces are not supported.

---

## Inspiration

Axer is a lightweight, cross-platform HTTP logging library for Kotlin Multiplatform projects.  
It’s inspired by [Chucker](https://github.com/ChuckerTeam/chucker) and [KtorMonitor](http://github.com/CosminMihuMDC/KtorMonitor/), with added multiplatform and Room DB inspection support.

---

## 💡 Tips & Extras

- For the latest desktop remote debugger, **check [Releases](https://github.com/orioneee/Axer/releases)**.
- You can mix and match all features or use only what you need.
- The API is intentionally minimal and consistent across platforms.

---

## License

MIT License
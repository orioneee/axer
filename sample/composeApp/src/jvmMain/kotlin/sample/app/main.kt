import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.orioneee.Axer
import io.github.orioneee.AxerOkhttpInterceptor
import io.github.orioneee.AxerWindows
import okhttp3.OkHttpClient
import org.koin.core.context.startKoin
import sample.app.App
import sample.app.koin.KoinModules
import java.awt.Dimension

val url = "https://pastebin.com/raw/Q315ARJ8?apiKey=test_api_key"


fun main() = application {
    Axer.installAxerErrorHandler()
    AxerWindows()
    startKoin {
        modules(KoinModules.module)
    }
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")

            if (original.header("Authorization") == null) {
                requestBuilder.header("Authorization", "Bearer your_token_here")
            }

            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(AxerOkhttpInterceptor.Builder()
            .setRequestImportantSelector { request ->
                listOf("request-url: ${request.method} path: ${request.path}")
            }
            .setResponseImportantSelector { response ->
                listOf("status: ${response.status}")
            }
            .setRequestFilter { request ->
                true
            }
            .setResponseFilter { response ->
                true
            }
            .setRequestReducer { request ->
                request.copy(path = "${request.path}?reduced=true")
            }
            .setResponseReducer { response ->
                response.copy(body = "REDACTED")
            }
            .build()
        )
        .build()

    Window(
        title = "sample",
        state = rememberWindowState(width = 350.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        Button(
            onClick = {
                val reuqest = client.newCall(
                    okhttp3.Request.Builder()
                        .url(url)
                        .build()
                ).execute()
            },
        ) {
            Text("Send get request")
        }
    }
}
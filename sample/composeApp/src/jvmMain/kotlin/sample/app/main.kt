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

fun main() = application {
    Axer.installAxerErrorHandler()
    AxerWindows()
    startKoin {
        modules(KoinModules.module)
    }
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalHeaders = originalRequest.headers

            val newRequestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")

            // Only add Authorization header if it's not already present
            if (originalHeaders["Authorization"] == null) {
                newRequestBuilder.header("Authorization", "c319c205-6601-432a-b269-1f654cf6d67b")
            }

            val newRequest = newRequestBuilder.build()
            chain.proceed(newRequest)
        }
        .addInterceptor(
            AxerOkhttpInterceptor.Builder()
                .build()
        )
        .build()
    Window(
        title = "sample",
        state = rememberWindowState(width = 350.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}
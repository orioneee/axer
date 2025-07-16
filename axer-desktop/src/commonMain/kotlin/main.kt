import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.orioneee.RemoteAxerDataProvider
import io.github.orioneee.presentation.AxerUIEntryPoint

fun main() = application {
    Window(
        title = "Axer Desktop",
        onCloseRequest = ::exitApplication,
    ) {
        val provider = remember { RemoteAxerDataProvider() }
        AxerUIEntryPoint().Screen(provider)
    }
}
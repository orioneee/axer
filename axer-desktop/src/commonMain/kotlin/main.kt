import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.RemoteAxerDataProvider
import io.github.orioneee.navigation.NavigationClass
import io.github.orioneee.presentation.AxerUIEntryPoint

fun main() = application {
    Window(
        title = "Axer Desktop",
        onCloseRequest = ::exitApplication,
    ) {
        NavigationClass().Host(rememberNavController())
    }
}
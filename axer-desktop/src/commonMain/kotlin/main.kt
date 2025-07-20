import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.RemoteAxerDataProvider
import io.github.orioneee.navigation.NavigationClass
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.presentation.components.AxerTheme

fun main() = application {
    Window(
        title = "Axer Desktop",
        onCloseRequest = ::exitApplication,
    ) {
        AxerTheme.ProvideTheme {
            Surface(
                modifier = Modifier.fillMaxSize()
            ){
                NavigationClass().Host(rememberNavController())
            }
        }
    }
}
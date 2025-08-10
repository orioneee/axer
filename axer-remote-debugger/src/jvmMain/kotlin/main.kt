import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.logo_circle
import io.github.orioneee.internal.presentation.components.AxerTheme
import io.github.orioneee.navigation.NavigationClass
import org.jetbrains.compose.resources.painterResource
import java.io.File

fun main() = application {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        e.printStackTrace()
        val file = File("error.txt")
        file.writeText(e.stackTraceToString())
    }
    val painter = painterResource(Res.drawable.logo_circle)
    Window(
        icon = painter,
        title = "Axer Desktop",
        onCloseRequest = ::exitApplication,
    ) {
        AxerTheme.ProvideTheme {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                NavigationClass().Host(rememberNavController())
            }
        }
    }
}
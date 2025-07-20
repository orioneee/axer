import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import io.github.orioneee.RemoteAxerDataProvider
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.logo_circle
import io.github.orioneee.navigation.NavigationClass
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.presentation.components.AxerTheme
import org.jetbrains.compose.resources.painterResource
import java.awt.Button
import java.awt.Dialog
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.Label
import java.io.File

fun main() = application {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        e.printStackTrace()
        //create file with error message and open it is system
        val file = File("error.txt")
        file.writeText(e.stackTraceToString() ?: "Unknown error")
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
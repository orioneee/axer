import androidx.compose.ui.window.ComposeUIViewController
import com.oriooneee.axer.Axer
import platform.UIKit.UIViewController
import sample.app.App


fun MainViewController(): UIViewController {
    Axer.installAxerErrorHandler()
    return ComposeUIViewController { App() }
}
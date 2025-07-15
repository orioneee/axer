import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.orioneee.RemoteAxerDataProvider

fun main() = application {
    Window(
        title = "Axer Monitor",
        onCloseRequest = ::exitApplication,
    ) {
        val provider = remember { RemoteAxerDataProvider() }
        val requests = provider.getAllRequests().collectAsState(emptyList())
        val exceptions = provider.getAllExceptions().collectAsState(emptyList())
        val logs = provider.getAllLogs().collectAsState(emptyList())
        val databases = provider.getDatabases().collectAsState(emptyList())
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Requests: ${requests.value.size}")
            Text("Exceptions: ${exceptions.value.size}")
            Text("Logs: ${logs.value.size}")
            Text("Databases: ${databases.value.size}")
        }
    }
}
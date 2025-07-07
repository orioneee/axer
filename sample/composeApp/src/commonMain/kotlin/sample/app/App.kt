package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.orioneee.Axer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import sample.app.room.SampleDatabase
import sample.app.room.entity.Director
import sample.app.room.entity.Movie
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val url = "https://pastebin.com/raw/Q315ARJ8?apiKey=test_api_key"

fun sendGetRequest(client: HttpClient) {
    CoroutineScope(Dispatchers.IO).launch {
        val resp = client.get(url)
        if (resp.status.value == 200) {
            val responseBody = resp.bodyAsText()
        } else {
        }
    }
}

fun sendPost(client: HttpClient) {
    val body =
        "{\"userPoints\":2450,\"nextLevel\":{\"name\":\"Data Pioneer\",\"pointsToLevelUp\":550}}"
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val resp = client.post(url) {
                setBody(body)
            }
            if (resp.status.value == 200) {
                val responseBody = resp.bodyAsText()
            } else {
            }
        } catch (e: Exception) {
        }
    }
}

fun sedRequestForImage(client: HttpClient) {
    val url = "https://www.super-hobby.ru/zdjecia/7/0/4/114_rd.jpg"
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val resp = client.get(url)
            if (resp.status.value == 200) {
                val responseBody = resp.bodyAsText()
                // Handle the image data as needed
            } else {
                // Handle message response
            }
        } catch (e: Exception) {
        }
    }
}

@OptIn(ExperimentalTime::class)
internal fun populateDatabase(database: SampleDatabase) {
    CoroutineScope(Dispatchers.IO).launch {
        val nameList = listOf(
            "John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona",
            "George", "Hannah", "Ian", "Julia", "Kevin", "Laura", "Mike", "Nina",
            "Oscar", "Paula", "Quentin", "Rachel"
        )
        val surnameList = listOf(
            "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller",
            "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White",
            "Harris", "Martin", "Thompson", "Garcia", "Martinez"
        )
        val directors = List(100_000) {
            val name = nameList.random()
            val surname = surnameList.random()
            Director(
                firstName = name,
                lastName = surname,
            )
        }
        val directorsDao = database.getDirectorDao()
        val startTime = Clock.System.now().toEpochMilliseconds()
        directors.chunked(50_000).forEachIndexed { index, it ->
            directorsDao.upsertDirectors(it)
            Axer.d("Sample", "Inserted page ${index + 1} of directors")
        }
        val endTime = Clock.System.now().toEpochMilliseconds()
        Axer.d("Sample", "Directors inserted in ${(endTime - startTime).div(1000.0)} seconds")
        val directorsFromDB = directorsDao.getAllDirectors()
        val movies = List(25) {
            val name = "Movie ${it + 1}"
            val director = directorsFromDB.random()
            Movie(
                title = name,
                directorId = director.id,
                releaseYear = (2000..2023).random(),
                rating = (10..50).random().toFloat().div(10f),
                description = "Description for $name",
                genre = listOf("Action", "Comedy", "Drama", "Horror", "Sci-Fi").random()
            )
        }
        val moviesDao = database.getMovieDao()
        moviesDao.upsertMovies(movies)
        Axer.d(
            "App",
            "Database populated with ${directors.size} directors and ${movies.size} movies."
        )
    }
}

@Composable
fun SwitchItem(
    text: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    var checked by remember { mutableStateOf(true) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = text, modifier = Modifier.align(Alignment.CenterVertically))
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange(it)
            },
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun App() {
    handlePermissions()
    val client = HttpClient {
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            if (!headers.contains("Authorization")) {
                header("Authorization", "Bearer your_token_here")
            }
        }
        install(Axer.ktorPlugin) {
            requestImportantSelector = {
                listOfNotNull(it.headers["Authorization"])
            }
        }
    }
    val database: SampleDatabase = koinInject()
    App(
        client = client,
        database = database,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
internal fun App(
    client: HttpClient,
    database: SampleDatabase,
    onOpenAxerUi: () -> Unit = { Axer.openAxerUI() }
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Axer Sample") },
                actions = {
                    IconButton(onClick = onOpenAxerUi) {
                        Icon(Icons.Default.BugReport, contentDescription = "Open Axer UI")
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                ActionCard("Network") {
                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = { sendGetRequest(client) }) {
                        Text("GET")
                    }
                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = { sendPost(client) }) {
                        Text("POST")
                    }
                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = { sedRequestForImage(client) }) {
                        Text("Image")
                    }
                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        sendGetRequest(client)
                        sendPost(client)
                        sedRequestForImage(client)
                    }) {
                        Text("All")
                    }
                }
            }

            item {
                ActionCard("Spam Ktor") {
                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = {
                            scope.launch { spamKtorMethods(client) }
                        }
                    ) { Text("Spam Methods") }

                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = {
                            scope.launch { spamKtorMedia(client) }
                        }
                    ) { Text("Spam Media") }

                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = {
                            scope.launch { spamKtorFormatsAndErrors(client) }
                        }
                    ) { Text("Spam Formats") }

                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = {
                            scope.launch { spamKtor(client) }
                        }
                    ) { Text("Spam All") }
                }
            }

            item {
                if (isSupportOkhttp()) {
                    ActionCard("Spam OkHttp") {
                        FilledTonalButton(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            onClick = {
                                scope.launch { spamOkHttpMethods() }
                            }
                        ) { Text("Spam Methods") }

                        FilledTonalButton(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            onClick = {
                                scope.launch { spamOkHttpMedia() }
                            }
                        ) { Text("Spam Media") }

                        FilledTonalButton(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            onClick = {
                                scope.launch { spamOkHttpFormatsAndErrors() }
                            }
                        ) { Text("Spam Formats") }

                        FilledTonalButton(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            onClick = {
                                scope.launch { spamOkHttp(client) }
                            }
                        ) { Text("Spam All") }
                    }
                }
            }



            item {
                ActionCard("Exceptions") {
                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = { Axer.recordException(Exception("Test non‑fatal")) }
                    ) { Text("Record Non‑Fatal") }

                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = { throw Exception("Test fatal") }) {
                        Text("Throw Fatal")
                    }
                }
            }

            /** ─── Database ─────────────────────────────────────────────── **/
            item {
                ActionCard("Database") {
                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        onClick = { populateDatabase(database) }) {
                        Text("Populate")
                    }
                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        scope.launch(Dispatchers.IO) {
                            database.getMovieDao().deleteAll()
                            database.getDirectorDao().deleteAll()
                        }
                    }) { Text("Clear") }
                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        scope.launch(Dispatchers.IO) {
                            val movies = database.getMovieDao().getAllMovies().size
                            val directors = database.getDirectorDao().getAllDirectors().size
                            Axer.d("Sample", "movies=$movies directors=$directors")
                        }
                    }) { Text("Counts") }
                }
            }

            /** ─── Logs ─────────────────────────────────────────────── **/
            item {
                ActionCard("Logs") {
                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        Axer.d("App", "Debug")
                    }) { Text("Debug") }

                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        Axer.i("App", "Info")
                    }) { Text("Info") }

                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        Axer.w("App", "Warn")
                    }) { Text("Warn") }

                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        Axer.e("App", "Error")
                    }) { Text("Error") }

                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        Axer.v("App", "Verbose")
                    }) { Text("Verbose") }

                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                        Axer.wtf("App", "Assert", Exception("Assert"))
                    }) { Text("Assert") }
                    FilledTonalButton(modifier = Modifier.padding(horizontal = 2.dp), onClick = {
                       CoroutineScope(Dispatchers.IO).launch {
                           val messages = List(500) {
                               "Log message number $it"
                           }
                           messages.forEach {
                               Axer.d("App", it)
                               Axer.i("App", it)
                               Axer.w("App", it)
                               Axer.e("App", it)
                               Axer.v("App", it)
                               Axer.wtf("App", it)
                               Axer.d("App", "--------------------------")
                           }
                       }
                    }) { Text("Spam logs") }
                }
            }

            item { MonitorToggles() }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) { content() }
        }
    }
}

@Composable
private fun MonitorToggles() {
    val config = remember { Axer.getConfig() }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Monitors",
                style = MaterialTheme.typography.titleMedium
            )

            SettingSwitch(
                label = "Requests",
                initialChecked = config.enableRequestMonitor
            ) { Axer.configure { enableRequestMonitor = it } }

            SettingSwitch(
                label = "Exceptions",
                initialChecked = config.enableExceptionMonitor
            ) { Axer.configure { enableExceptionMonitor = it } }

            SettingSwitch(
                label = "Logs",
                initialChecked = config.enableLogMonitor
            ) { Axer.configure { enableLogMonitor = it } }

            SettingSwitch(
                label = "Database",
                initialChecked = config.enableDatabaseMonitor
            ) { Axer.configure { enableDatabaseMonitor = it } }

            SettingSwitch(
                label = "Record Logs",
                initialChecked = config.isRecordingLogs
            ) { Axer.configure { isRecordingLogs = it } }
        }
    }
}


@Composable
private fun SettingSwitch(
    label: String,
    initialChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    var checked by remember { mutableStateOf(initialChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            //‑‑ makes the whole row behave like a Switch
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = { isChecked ->
                    checked = isChecked
                    onCheckedChange(isChecked)
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}
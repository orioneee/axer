package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.orioneee.Axer
import io.github.orioneee.domain.SupportedLocales
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
        directorsDao.upsertDirectors(directors)
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
        println("Database populated with ${directors.size} directors and ${movies.size} movies.")
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
        install(Axer.ktorPlugin){
            retentionPeriodInSeconds = 60 * 60 * 1
        }
    }
    val database: SampleDatabase = koinInject()
    Surface {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            columns = GridCells.Adaptive(minSize = 200.dp)
        ) {
            item {
                Button(
                    onClick = {
                        sendGetRequest(client)
                    }
                ) {
                    Text("Send get request")
                }
            }
            item {
                Button(
                    onClick = {
                        sendPost(client)
                    }
                ) {
                    Text("Send post request")
                }
            }
            item {
                Button(
                    onClick = {
                        sedRequestForImage(client)
                    }
                ) {
                    Text("Send request for image")
                }
            }
            item {
                Button(
                    onClick = {
                        sendGetRequest(client)
                        sendPost(client)
                        sedRequestForImage(client)
                    }
                ) {
                    Text("Send all requests")
                }
            }
            item {
                Button(
                    onClick = {
                        Axer.recordException(Exception("Test non-fatal exception"))
                    }
                ) {
                    Text("Record non fatal exception")
                }
            }
            item {
                Button(
                    onClick = {
                        throw Exception("Test fatal exception")
                    }
                ) {
                    Text("Throw fatal exception")
                }
            }
            item {
                Button(
                    onClick = {
                        Axer.openAxerUI()
                    }
                ) {
                    Text("Open Axer UI")
                }
            }
            item {
                Button(
                    onClick = {
                        populateDatabase(database)
                    }
                ) {
                    Text("Populate database")
                }
            }
            item {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.getMovieDao().deleteAll()
                            database.getDirectorDao().deleteAll()
                        }
                    }
                ) {
                    Text("Clear database")
                }
            }
            item {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            database.getMovieDao().getAllMovies().let {
                                println("Movies in database: ${it.size}")
                            }
                            println(
                                "Directors in database: ${
                                    database.getDirectorDao().getAllDirectors().size
                                }"
                            )
                        }
                    }
                ) {
                    Text("Get all movies and directors")
                }
            }
            item {
                Button(
                    onClick = {
                        Axer.d("App", "Test debug log")
                        Axer.e("App", "Test error log")
                        Axer.i("App", "Test info log")
                        Axer.w(
                            "Tag2",
                            "Test warning log",
                            record = false
                        )
                        Axer.v("Tag2", "Test verbose log")
                        Axer.wtf("App", "Test assert log", Exception("Test assert exception"))
                    }
                ) {
                    Text("Test logs")
                }
            }
            item {
                SwitchItem(
                    text = "Enable requests",
                    onCheckedChange = {
                        Axer.configure {
                            enableRequestMonitor = it
                        }
                    }
                )
            }
            item {
                SwitchItem(
                    text = "Enable exceptions",
                    onCheckedChange = {
                        Axer.configure {
                            enableExceptionMonitor = it
                        }
                    }
                )
            }
            item {
                SwitchItem(
                    text = "Enable logs",
                    onCheckedChange = {
                        Axer.configure {
                            enableLogMonitor = it
                        }
                    }
                )
            }
            item {
                SwitchItem(
                    text = "Enable database",
                    onCheckedChange = {
                        Axer.configure {
                            enableDatabaseMonitor = it
                        }
                    }
                )
            }
        }
    }
}
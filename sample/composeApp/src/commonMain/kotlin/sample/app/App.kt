package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun App() {
    Axer.initializeLogger()
    handlePermissions()
    val client = HttpClient {
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
            if (!headers.contains("Authorization")) {
                header("Authorization", "Bearer your_token_here")
            }
        }
        install(Axer.ktorPlugin)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                sendGetRequest(client)
            }
        ) {
//            Axer.changeLocale(SupportedLocales.ENGLISH)
            Text("Send get request")
        }
        Button(
            onClick = {
//                Axer.changeLocale(SupportedLocales.UKRAINIAN)
                sendPost(client)
            }
        ) {
            Text("Send post request")
        }
        Button(
            onClick = {
//                Axer.changeLocale(SupportedLocales.RUSSIAN)
                sedRequestForImage(client)
            }
        ) {
            Text("Send request for image")
        }

        Button(
            onClick = {
                sendGetRequest(client)
                sendPost(client)
                sedRequestForImage(client)
            }
        ) {
            Text("Send all requests")
        }

        Button(
            onClick = {
                Axer.recordException(Exception("Test non-fatal exception"))
            }
        ) {
            Text("Record non fatal exception")
        }

        Button(
            onClick = {
                throw Exception("Test fatal exception")
            }
        ) {
            Text("Throw fatal exception")
        }
        Button(
            onClick = {
                Axer.openAxerUI()
            }
        ) {
            Text("Open Axer UI")
        }
        val database: SampleDatabase = koinInject()
        Button(
            onClick = {
                populateDatabase(database)
            }
        ) {
            Text("Populate database")
        }
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
        Button(
            onClick = {
                Axer.d("App", "Test debug log")
//                Axer.d("App", "Test debug log with throwable", Throwable("Test throwable"))

                Axer.e("App", "Test error log")
//                Axer.e("App", "Test error log with throwable", Throwable("Test throwable"))

                Axer.i("App", "Test info log")
//                Axer.i("App", "Test info log with throwable", Throwable("Test throwable"))

                Axer.w(
                    "Tag2",
                    "Test warning log",
                    record = false
                ) // This will not be recorded in Axer but will be logged
//                Axer.w("App", "Test warning log with throwable", Throwable("Test throwable"))

                Axer.v("Tag2", "Test verbose log")
//                Axer.v("App", "Test verbose log with throwable", Throwable("Test throwable"))

                Axer.wtf("App", "Test assert log")
                Axer.wtf("Tag3", "Test assert log with throwable", Throwable("Test throwable"))
            }
        ) {
            Text("Test logs")
        }
    }
}
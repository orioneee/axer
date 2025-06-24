package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.oriooneee.axer.Axer
import com.oriooneee.axer.AxerPlugin
import com.oriooneee.axer.domain.requests.Request
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

val url = "https://pastebin.com/raw/CNsie2wb?apiKey=test_api_key"


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
            e.printStackTrace()
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
                // Handle error response
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        install(AxerPlugin) {
            requestImportantSelector = { request: Request ->
                val tokenData = request.headers.entries.firstOrNull {
                    it.key.equals("Authorization", ignoreCase = true)
                }
                if (tokenData != null) {
                    listOf(tokenData.value)
                } else {
                    emptyList()
                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                sendGetRequest(client)
            }
        ) {
            Text("Send get request")
        }
        Button(
            onClick = {
                sendPost(client)
            }
        ) {
            Text("Send post request")
        }
        Button(
            onClick = {
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
    }
}
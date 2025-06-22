import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.oriooneee.axer.AxerOkhttpInterceptor
import com.oriooneee.axer.AxerWindows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import sample.app.App
import java.awt.Dimension

fun sendGetRequestOkHttp(
    client: OkHttpClient
) {
    val request = Request.Builder()
        .url("https://purple-connex-da7fa5d14c63.herokuapp.com/v2/user/profile")
        .header("Accept", "application/json")
        .build()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("GET response: $responseBody")
                } else {
                    println("GET failed with code: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendPostRequestOkHttp(
    client: OkHttpClient
) {
    val jsonBody = """
        {
            "userPoints": 2450,
            "nextLevel": {
                "name": "Data Pioneer",
                "pointsToLevelUp": 550
            }
        }
    """.trimIndent()

    val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
    val body = jsonBody.toRequestBody(mediaTypeJson)

    val request = Request.Builder()
        .url("https://pastebin.com/raw/CNsie2wb")
        .post(body)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("POST response: $responseBody")
                } else {
                    println("POST failed with code: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun sendImageRequestOkHttp(
    client: OkHttpClient
) {
    val imageUrl = "https://www.super-hobby.ru/zdjecia/7/0/4/114_rd.jpg"

    val request = Request.Builder()
        .url(imageUrl)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val imageBytes = response.body?.bytes()
                    println("Image downloaded, size: ${imageBytes?.size} bytes")
                    // You can save or display the image here
                } else {
                    println("Image request failed with code: ${response.code}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}




fun main() = application {
    AxerWindows()
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val originalHeaders = originalRequest.headers

            val newRequestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")

            // Only add Authorization header if it's not already present
            if (originalHeaders["Authorization"] == null) {
                newRequestBuilder.header("Authorization", "c319c205-6601-432a-b269-1f654cf6d67b")
            }

            val newRequest = newRequestBuilder.build()
            chain.proceed(newRequest)
        }
        .addInterceptor(
            AxerOkhttpInterceptor.Builder()
                .build()
        )
        .build()
    Window(
        title = "sample",
        state = rememberWindowState(width = 350.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
//        Column(
//            modifier = Modifier.fillMaxSize().background(Color.White),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
//        ) {
//            Button(
//                onClick = {
//                    sendGetRequestOkHttp(client)
//                }
//            ) {
//                Text("Send get request")
//            }
//            Button(
//                onClick = {
//                    sendGetRequestOkHttp(client)
//                }
//            ) {
//                Text("Send post request")
//            }
//            Button(
//                onClick = {
//                    sendImageRequestOkHttp(client)
//                }
//            ) {
//                Text("Send request for image")
//            }
//
//            Button(
//                onClick = {
//                    sendGetRequestOkHttp(client)
//                    sendPostRequestOkHttp(client)
//                    sendImageRequestOkHttp(client)
//                }
//            ) {
//                Text("Send all requests")
//            }
        App()
    }
}
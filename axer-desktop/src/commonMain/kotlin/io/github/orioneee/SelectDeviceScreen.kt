package io.github.orioneee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.orioneee.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.util.function.IntFunction
import java.util.stream.IntStream


class SelectDeviceScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(navController: NavHostController) {
        val coroutineScope = rememberCoroutineScope()
        var devices by remember { mutableStateOf<List<String>>(emptyList()) }
        var isSearching by remember { mutableStateOf(true) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Device") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                when {
                    isSearching -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Searching for devices...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    devices.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No devices found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(devices) { ip ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate("${Route.DEVICE_INSPECTION.path}/$ip")
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Devices,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = ip,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            devices = scanForAxerDevices()
            isSearching = false
        }
    }


    suspend fun scanForAxerDevices(): List<String> = withContext(Dispatchers.IO) {
        val reachableIps = mutableListOf<String>()

        // Try to get local subnet like 192.168.1
        val subnet = try {
            val localHost = InetAddress.getLocalHost()
            val ipAddr = localHost.address
            String.format(
                "%d.%d.%d",
                (ipAddr[0].toInt() and 0xFF),
                (ipAddr[1].toInt() and 0xFF),
                (ipAddr[2].toInt() and 0xFF)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }

        // Create and launch parallel ping + HTTP check
        val jobs = (1..254).map { i ->
            async {
                val ip = "$subnet.$i"
                try {
                    val inet = InetAddress.getByName(ip)
                    if (inet.isReachable(100)) {
                        val url = "http://$ip:9000/isAxerServer"
                        println("Checking $url")
                        val connection = URL(url).openConnection() as HttpURLConnection
                        connection.connectTimeout = 1000
                        connection.readTimeout = 1000
                        connection.requestMethod = "GET"
                        val response = connection.inputStream.bufferedReader().readText()
                        connection.disconnect()
                        if (response.contains("Axer")) {
                            synchronized(reachableIps) {
                                reachableIps.add(ip)
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore all network errors
                }
            }
        }

        jobs.awaitAll()
        return@withContext reachableIps
    }
}

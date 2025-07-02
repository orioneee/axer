package io.github.orioneee.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlignVerticalCenter
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class FlowDestinations(
    val icon: ImageVector,
    val label: String,
    val route: Routes,
) {
    REQUESTS_FLOW(
        icon = Icons.Outlined.NetworkCheck,
        label = "Requests",
        route = Routes.REQUESTS_FLOW
    ),
    EXCEPTIONS_FLOW(
        icon = Icons.Outlined.Info,
        label = "Exceptions",
        route = Routes.EXCEPTIONS_FLOW
    ),
    LOG_VIEW(
        icon = Icons.Outlined.AlignVerticalCenter,
        label = "Logs",
        route = Routes.LOG_VIEW
    ),
    DATABASE_FLOW(
        icon = Icons.Outlined.Dataset,
        label = "Database",
        route = Routes.DATABASE_FLOW
    ),
}
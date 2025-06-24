package com.oriooneee.axer.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class FlowDestinations(
    val icon: ImageVector,
    val label: String,
    val route: Routes,
) {
    REQUESTS_FLOW(
        icon = Icons.Outlined.NetworkCell,
        label = "Requests",
        route = Routes.REQUESTS_FLOW
    ),
    EXCEPTIONS_FLOW(
        icon = Icons.Outlined.SmsFailed,
        label = "Exceptions",
        route = Routes.EXCEPTIONS_FLOW
    )
}
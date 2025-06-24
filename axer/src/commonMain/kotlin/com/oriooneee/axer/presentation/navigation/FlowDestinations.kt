package com.oriooneee.axer.presentation.navigation

import androidx.compose.material.icons.Icons
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
    )
}
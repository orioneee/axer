package com.oriooneee.axer.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.SmsFailed
import androidx.compose.ui.graphics.vector.ImageVector

enum class FlowDestinations(val icon: ImageVector, val label: String) {
    REQUESTS_FLOW(
        icon = Icons.Outlined.NetworkCell,
        label = "Requests"
    ),
    EXCEPTIONS_FLOW(
        icon = Icons.Outlined.SmsFailed,
        label = "Exceptions"
    )
}
package io.github.orioneee.internal.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.database
import io.github.orioneee.axer.generated.resources.exceptions
import io.github.orioneee.axer.generated.resources.logs
import io.github.orioneee.axer.generated.resources.requests
import org.jetbrains.compose.resources.StringResource

internal enum class FlowDestinations(
    val icon: ImageVector,
    val label: StringResource,
    val route: String,
) {
    REQUESTS_FLOW(
        icon = Icons.Outlined.NetworkCheck,
        label = Res.string.requests,
        route = Routes.REQUESTS_FLOW.route
    ),
    EXCEPTIONS_FLOW(
        icon = Icons.Outlined.Info,
        label = Res.string.exceptions,
        route = Routes.EXCEPTIONS_FLOW.route
    ),
    LOG_VIEW(
        icon = Icons.AutoMirrored.Outlined.Article,
        label = Res.string.logs,
        route = Routes.LOG_VIEW.route
    ),
    DATABASE_FLOW(
        icon = Icons.Outlined.Dataset,
        label = Res.string.database,
        route = Routes.DATABASE_FLOW.route
    ),
}
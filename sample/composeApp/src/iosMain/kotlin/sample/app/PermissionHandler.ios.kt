package sample.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION

actual object PermissionHandler {
    @Composable
    actual fun handlePermissions() {
        val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
        val controller: PermissionsController =
            remember(factory) { factory.createPermissionsController() }
        BindEffect(controller)
        LaunchedEffect(Unit) {
            try {
                controller.providePermission(Permission.REMOTE_NOTIFICATION)
            } catch (deniedAlways: DeniedAlwaysException) {
            } catch (denied: DeniedException) {
            } catch (e: Exception) {
            }
        }
    }
}
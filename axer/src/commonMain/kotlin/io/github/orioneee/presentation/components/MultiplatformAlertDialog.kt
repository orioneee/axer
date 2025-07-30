package io.github.orioneee.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.close
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun MultiplatformAlertDialog(
    canDismissByClickOutside: Boolean = true,
    isShowDialog: Boolean,
    onDismiss: () -> Unit,
    title: @Composable (() -> Unit),
    cancelButton: @Composable (() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit) = {
        TextButton(
            onClick = onDismiss
        ) {
            Text(stringResource(Res.string.close))
        }
    },
    content: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplatformAlertDialog(
    state: MutableState<Boolean>,
    title: @Composable (() -> Unit),
    confirmButton: @Composable (() -> Unit) = {
        TextButton(
            onClick = {
                state.value = false
            }
        ) {
            Text(stringResource(Res.string.close))
        }
    },
    content: @Composable () -> Unit,
){
    MultiplatformAlertDialog(
        isShowDialog = state.value,
        onDismiss = { state.value = false },
        title = title,
        confirmButton = confirmButton,
        content = content
    )
}

